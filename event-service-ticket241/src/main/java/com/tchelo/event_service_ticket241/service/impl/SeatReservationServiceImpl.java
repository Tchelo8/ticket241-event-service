package com.tchelo.event_service_ticket241.service.impl;

import com.tchelo.event_service_ticket241.entity.Event;
import com.tchelo.event_service_ticket241.entity.ProcessedKafkaMessage;
import com.tchelo.event_service_ticket241.entity.SeatReservation;
import com.tchelo.event_service_ticket241.entity.TicketCategory;
import com.tchelo.event_service_ticket241.entity.enums.SeatReservationStatus;
import com.tchelo.event_service_ticket241.exception.EventNotFoundException;
import com.tchelo.event_service_ticket241.exception.TicketCategoryNotFoundException;
import com.tchelo.event_service_ticket241.repository.EventRepository;
import com.tchelo.event_service_ticket241.repository.ProcessedKafkaMessageRepository;
import com.tchelo.event_service_ticket241.repository.SeatReservationRepository;
import com.tchelo.event_service_ticket241.repository.TicketCategoryRepository;
import com.tchelo.event_service_ticket241.service.SeatReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

// Verrous : toujours l'événement puis la catégorie (findByIdForUpdate), jamais l'inverse,
// pour qu'aucun interblocage ne soit possible entre deux commandes du même événement.
@Slf4j
@Service
@RequiredArgsConstructor
public class SeatReservationServiceImpl implements SeatReservationService {

    private final SeatReservationRepository seatReservationRepository;
    private final ProcessedKafkaMessageRepository processedKafkaMessageRepository;
    private final EventRepository eventRepository;
    private final TicketCategoryRepository ticketCategoryRepository;

    @Override
    @Transactional
    public ReservationOutcome reserve(String orderId, Long categoryId, Integer quantity) {
        Optional<SeatReservation> existing = seatReservationRepository.findByOrderIdForUpdate(orderId);
        if (existing.isPresent()) {
            log.info("Commande {} déjà traitée ({}), même réponse renvoyée", orderId, existing.get().getStatus());
            return outcomeOf(existing.get());
        }

        if (quantity == null || quantity <= 0) {
            return reject(orderId, null, categoryId, quantity, "Quantité de billets invalide");
        }

        Long eventId = ticketCategoryRepository.findEventIdByCategoryId(categoryId).orElse(null);
        if (eventId == null) {
            return reject(orderId, null, categoryId, quantity, "Catégorie de billet introuvable");
        }

        Event event = lockEvent(eventId);
        TicketCategory category = lockCategory(categoryId);

        String saleClosedReason = event.getSaleClosedReason();
        if (saleClosedReason != null) {
            return reject(orderId, eventId, categoryId, quantity, saleClosedReason);
        }
        if (!Boolean.TRUE.equals(category.getIsActive())) {
            return reject(orderId, eventId, categoryId, quantity, "Cette catégorie de billet n'est plus en vente");
        }
        if (category.getMaxTicketsPerOrder() != null && quantity > category.getMaxTicketsPerOrder()) {
            return reject(orderId, eventId, categoryId, quantity,
                    "Maximum " + category.getMaxTicketsPerOrder() + " billet(s) par commande dans cette catégorie");
        }
        if (!category.hasAvailableSeats(quantity)) {
            return reject(orderId, eventId, categoryId, quantity, String.format(
                    "Plus assez de places dans la catégorie %s (disponibles : %d, demandées : %d)",
                    category.getName(), category.getAvailableSeats(), quantity));
        }

        category.reserveSeats(quantity);
        event.recalculateSeatsFromCategories();

        seatReservationRepository.save(SeatReservation.builder()
                .orderId(orderId)
                .eventId(eventId)
                .ticketCategoryId(categoryId)
                .requestedQuantity(quantity)
                .reservedQuantity(quantity)
                .soldQuantity(0)
                .status(SeatReservationStatus.ACCEPTED)
                .build());

        log.info("{} place(s) réservée(s) pour la commande {} (catégorie {})", quantity, orderId, categoryId);
        return new ReservationOutcome(true, eventId, "Places réservées avec succès");
    }

    @Override
    @Transactional
    public void releaseReservedTicket(String orderId, Long ticketId) {
        if (alreadyProcessed("release-reserved:" + ticketId)) {
            return;
        }

        Optional<SeatReservation> found = seatReservationRepository.findByOrderIdForUpdate(orderId);
        if (found.isEmpty()) {
            // La commande est close avant que sa réservation n'arrive : on la marque fermée pour
            // qu'une réservation tardive soit refusée au lieu de bloquer des places pour rien
            seatReservationRepository.save(SeatReservation.builder()
                    .orderId(orderId)
                    .requestedQuantity(0)
                    .status(SeatReservationStatus.CLOSED)
                    .failureReason("Commande close avant la réservation des places")
                    .build());
            log.info("Commande {} close avant toute réservation de places", orderId);
            return;
        }

        SeatReservation reservation = found.get();
        if (reservation.getStatus() != SeatReservationStatus.ACCEPTED || reservation.getReservedQuantity() < 1) {
            log.info("Rien à libérer pour le billet {} de la commande {} ({}, {} place(s) réservée(s))",
                    ticketId, orderId, reservation.getStatus(), reservation.getReservedQuantity());
            return;
        }

        Event event = lockEvent(reservation.getEventId());
        TicketCategory category = lockCategory(reservation.getTicketCategoryId());
        category.cancelReservation(1);
        event.recalculateSeatsFromCategories();
        reservation.setReservedQuantity(reservation.getReservedQuantity() - 1);

        log.info("Place réservée libérée : billet {} de la commande {}", ticketId, orderId);
    }

    @Override
    @Transactional
    public void confirmPaidOrder(String orderId, Integer quantity) {
        if (alreadyProcessed("ticket-paid:" + orderId)) {
            return;
        }

        SeatReservation reservation = seatReservationRepository.findByOrderIdForUpdate(orderId).orElse(null);
        if (reservation == null || reservation.getStatus() != SeatReservationStatus.ACCEPTED) {
            log.error("Paiement reçu pour la commande {} sans réservation de places active : aucune place vendue", orderId);
            return;
        }

        int requested = quantity == null ? reservation.getReservedQuantity() : quantity;
        int toConfirm = Math.min(requested, reservation.getReservedQuantity());
        if (toConfirm < requested) {
            log.error("Commande {} : {} place(s) payée(s) mais seulement {} encore réservée(s)",
                    orderId, requested, toConfirm);
        }
        if (toConfirm == 0) {
            return;
        }

        Event event = lockEvent(reservation.getEventId());
        TicketCategory category = lockCategory(reservation.getTicketCategoryId());
        category.confirmSale(toConfirm);
        event.recalculateSeatsFromCategories();
        reservation.setReservedQuantity(reservation.getReservedQuantity() - toConfirm);
        reservation.setSoldQuantity(reservation.getSoldQuantity() + toConfirm);

        log.info("{} place(s) vendue(s) pour la commande {}", toConfirm, orderId);
    }

    @Override
    @Transactional
    public void releaseSoldTicket(String orderId, Long ticketId) {
        if (alreadyProcessed("release-sold:" + ticketId)) {
            return;
        }

        SeatReservation reservation = seatReservationRepository.findByOrderIdForUpdate(orderId).orElse(null);
        if (reservation == null || reservation.getSoldQuantity() < 1) {
            log.warn("Aucune place vendue à libérer pour le billet {} de la commande {}", ticketId, orderId);
            return;
        }

        Event event = lockEvent(reservation.getEventId());
        TicketCategory category = lockCategory(reservation.getTicketCategoryId());
        category.cancelSale(1);
        event.recalculateSeatsFromCategories();
        reservation.setSoldQuantity(reservation.getSoldQuantity() - 1);

        log.info("Place vendue libérée : billet {} de la commande {}", ticketId, orderId);
    }

    // Enregistre la clé du message ; renvoie true si elle l'était déjà (message rejoué)
    private boolean alreadyProcessed(String messageKey) {
        if (processedKafkaMessageRepository.existsById(messageKey)) {
            log.info("Message déjà traité, ignoré : {}", messageKey);
            return true;
        }
        processedKafkaMessageRepository.save(new ProcessedKafkaMessage(messageKey, LocalDateTime.now()));
        return false;
    }

    private Event lockEvent(Long eventId) {
        return eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new EventNotFoundException("Événement non trouvé avec l'ID : " + eventId));
    }

    private TicketCategory lockCategory(Long categoryId) {
        return ticketCategoryRepository.findByIdForUpdate(categoryId)
                .orElseThrow(() -> new TicketCategoryNotFoundException(
                        "Catégorie de ticket non trouvée avec l'ID : " + categoryId));
    }

    private ReservationOutcome reject(String orderId, Long eventId, Long categoryId, Integer quantity, String reason) {
        seatReservationRepository.save(SeatReservation.builder()
                .orderId(orderId)
                .eventId(eventId)
                .ticketCategoryId(categoryId)
                .requestedQuantity(quantity == null ? 0 : quantity)
                .status(SeatReservationStatus.REJECTED)
                .failureReason(reason)
                .build());
        log.info("Réservation refusée pour la commande {} : {}", orderId, reason);
        return new ReservationOutcome(false, eventId, reason);
    }

    private ReservationOutcome outcomeOf(SeatReservation reservation) {
        return switch (reservation.getStatus()) {
            case ACCEPTED -> new ReservationOutcome(true, reservation.getEventId(), "Places réservées avec succès");
            case REJECTED, CLOSED -> new ReservationOutcome(false, reservation.getEventId(), reservation.getFailureReason());
        };
    }
}
