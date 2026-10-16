package com.tchelo.event_service_ticket241.service.impl;

import com.tchelo.event_service_ticket241.entity.Event;
import com.tchelo.event_service_ticket241.entity.ProcessedKafkaMessage;
import com.tchelo.event_service_ticket241.entity.SeatReservation;
import com.tchelo.event_service_ticket241.entity.SeatReservationLine;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

// Verrous : toujours l'événement puis ses catégories par id croissant (findByIdForUpdate), jamais
// dans un autre ordre, pour qu'aucun interblocage ne soit possible entre deux commandes du même événement.
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
    public ReservationOutcome reserve(String orderId, List<ReservationLine> lines) {
        Optional<SeatReservation> existing = seatReservationRepository.findByOrderIdForUpdate(orderId);
        if (existing.isPresent()) {
            log.info("Commande {} déjà traitée ({}), même réponse renvoyée", orderId, existing.get().getStatus());
            return outcomeOf(existing.get());
        }

        // Une même catégorie citée deux fois compte une seule fois (quantités additionnées) ;
        // TreeMap : catégories parcourues (et verrouillées) par id croissant
        Map<Long, Integer> quantities = new TreeMap<>();
        for (ReservationLine line : lines == null ? List.<ReservationLine>of() : lines) {
            if (line.categoryId() == null || line.quantity() == null || line.quantity() <= 0) {
                return reject(orderId, null, quantities, "Quantité de billets invalide");
            }
            quantities.merge(line.categoryId(), line.quantity(), Integer::sum);
        }
        if (quantities.isEmpty()) {
            return reject(orderId, null, quantities, "Commande sans billet");
        }

        // Toutes les catégories doivent appartenir au même événement
        Long eventId = null;
        for (Long categoryId : quantities.keySet()) {
            Long categoryEventId = ticketCategoryRepository.findEventIdByCategoryId(categoryId).orElse(null);
            if (categoryEventId == null) {
                return reject(orderId, eventId, quantities, "Catégorie de billet introuvable");
            }
            if (eventId != null && !eventId.equals(categoryEventId)) {
                return reject(orderId, eventId, quantities, "Toutes les catégories d'une commande doivent être du même événement");
            }
            eventId = categoryEventId;
        }

        Event event = lockEvent(eventId);
        String saleClosedReason = event.getSaleClosedReason();
        if (saleClosedReason != null) {
            return reject(orderId, eventId, quantities, saleClosedReason);
        }

        // Tout vérifier AVANT de toucher aux compteurs : tout-ou-rien
        Map<Long, TicketCategory> categories = new LinkedHashMap<>();
        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            TicketCategory category = lockCategory(entry.getKey());
            int quantity = entry.getValue();
            if (!Boolean.TRUE.equals(category.getIsActive())) {
                return reject(orderId, eventId, quantities,
                        "La catégorie " + category.getName() + " n'est plus en vente");
            }
            if (category.getMaxTicketsPerOrder() != null && quantity > category.getMaxTicketsPerOrder()) {
                return reject(orderId, eventId, quantities, "Maximum " + category.getMaxTicketsPerOrder()
                        + " billet(s) par commande dans la catégorie " + category.getName());
            }
            if (!category.hasAvailableSeats(quantity)) {
                return reject(orderId, eventId, quantities, String.format(
                        "Plus assez de places dans la catégorie %s (disponibles : %d, demandées : %d)",
                        category.getName(), category.getAvailableSeats(), quantity));
            }
            categories.put(entry.getKey(), category);
        }

        SeatReservation reservation = newReservation(orderId, eventId, quantities, SeatReservationStatus.ACCEPTED, null);
        for (Map.Entry<Long, TicketCategory> entry : categories.entrySet()) {
            int quantity = quantities.get(entry.getKey());
            entry.getValue().reserveSeats(quantity);
            reservation.getLines().add(SeatReservationLine.builder()
                    .reservation(reservation)
                    .ticketCategoryId(entry.getKey())
                    .requestedQuantity(quantity)
                    .reservedQuantity(quantity)
                    .soldQuantity(0)
                    .build());
        }
        event.recalculateSeatsFromCategories();
        reservation.recalculateTotals();
        seatReservationRepository.save(reservation);

        log.info("{} place(s) réservée(s) pour la commande {} ({})", reservation.getReservedQuantity(), orderId, quantities);
        return new ReservationOutcome(true, eventId, "Places réservées avec succès");
    }

    @Override
    @Transactional
    public void releaseReservedTicket(String orderId, Long ticketId, Long categoryId) {
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
        if (reservation.getStatus() != SeatReservationStatus.ACCEPTED) {
            log.info("Rien à libérer pour le billet {} de la commande {} ({})", ticketId, orderId, reservation.getStatus());
            return;
        }
        SeatReservationLine line = reservation.lineFor(categoryId).orElse(null);
        if (line == null || line.getReservedQuantity() < 1) {
            log.info("Rien à libérer pour le billet {} de la commande {} (catégorie {})", ticketId, orderId, categoryId);
            return;
        }

        Event event = lockEvent(reservation.getEventId());
        lockCategory(line.getTicketCategoryId()).cancelReservation(1);
        event.recalculateSeatsFromCategories();
        line.setReservedQuantity(line.getReservedQuantity() - 1);
        reservation.recalculateTotals();

        log.info("Place réservée libérée : billet {} de la commande {} (catégorie {})",
                ticketId, orderId, line.getTicketCategoryId());
    }

    @Override
    @Transactional
    public void confirmPaidOrder(String orderId, List<ReservationLine> paidLines) {
        if (alreadyProcessed("ticket-paid:" + orderId)) {
            return;
        }

        SeatReservation reservation = seatReservationRepository.findByOrderIdForUpdate(orderId).orElse(null);
        if (reservation == null || reservation.getStatus() != SeatReservationStatus.ACCEPTED) {
            log.error("Paiement reçu pour la commande {} sans réservation de places active : aucune place vendue", orderId);
            return;
        }

        // Quantité payée par ligne : celle du message, ou tout ce qui est encore réservé
        Map<SeatReservationLine, Integer> toConfirm = new LinkedHashMap<>();
        if (paidLines == null) {
            reservation.resolveLines().forEach(line -> toConfirm.put(line, line.getReservedQuantity()));
        } else {
            for (ReservationLine paid : paidLines) {
                SeatReservationLine line = reservation.lineFor(paid.categoryId()).orElse(null);
                if (line == null) {
                    log.error("Commande {} : {} place(s) payée(s) dans la catégorie {} qui n'en fait pas partie",
                            orderId, paid.quantity(), paid.categoryId());
                    continue;
                }
                toConfirm.merge(line, paid.quantity() == null ? line.getReservedQuantity() : paid.quantity(), Integer::sum);
            }
        }

        Event event = lockEvent(reservation.getEventId());
        // Ordre des verrous : catégories par id croissant
        toConfirm.entrySet().stream()
                .sorted(Map.Entry.comparingByKey((a, b) -> a.getTicketCategoryId().compareTo(b.getTicketCategoryId())))
                .forEach(entry -> confirmLine(orderId, entry.getKey(), entry.getValue()));
        event.recalculateSeatsFromCategories();
        reservation.recalculateTotals();
    }

    private void confirmLine(String orderId, SeatReservationLine line, int requested) {
        int quantity = Math.min(requested, line.getReservedQuantity());
        if (quantity < requested) {
            log.error("Commande {} : {} place(s) payée(s) mais seulement {} encore réservée(s) dans la catégorie {}",
                    orderId, requested, quantity, line.getTicketCategoryId());
        }
        if (quantity <= 0) {
            return;
        }
        lockCategory(line.getTicketCategoryId()).confirmSale(quantity);
        line.setReservedQuantity(line.getReservedQuantity() - quantity);
        line.setSoldQuantity(line.getSoldQuantity() + quantity);
        log.info("{} place(s) vendue(s) pour la commande {} (catégorie {})", quantity, orderId, line.getTicketCategoryId());
    }

    @Override
    @Transactional
    public void releaseSoldTicket(String orderId, Long ticketId, Long categoryId) {
        if (alreadyProcessed("release-sold:" + ticketId)) {
            return;
        }

        SeatReservation reservation = seatReservationRepository.findByOrderIdForUpdate(orderId).orElse(null);
        SeatReservationLine line = reservation == null ? null : reservation.lineFor(categoryId).orElse(null);
        if (line == null || line.getSoldQuantity() < 1) {
            log.warn("Aucune place vendue à libérer pour le billet {} de la commande {} (catégorie {})",
                    ticketId, orderId, categoryId);
            return;
        }

        Event event = lockEvent(reservation.getEventId());
        lockCategory(line.getTicketCategoryId()).cancelSale(1);
        event.recalculateSeatsFromCategories();
        line.setSoldQuantity(line.getSoldQuantity() - 1);
        reservation.recalculateTotals();

        log.info("Place vendue libérée : billet {} de la commande {} (catégorie {})",
                ticketId, orderId, line.getTicketCategoryId());
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

    private SeatReservation newReservation(String orderId, Long eventId, Map<Long, Integer> quantities,
                                           SeatReservationStatus status, String failureReason) {
        return SeatReservation.builder()
                .orderId(orderId)
                .eventId(eventId)
                .ticketCategoryId(quantities.size() == 1 ? quantities.keySet().iterator().next() : null)
                .requestedQuantity(quantities.values().stream().mapToInt(Integer::intValue).sum())
                .status(status)
                .failureReason(failureReason)
                .build();
    }

    private ReservationOutcome reject(String orderId, Long eventId, Map<Long, Integer> quantities, String reason) {
        seatReservationRepository.save(newReservation(orderId, eventId, quantities, SeatReservationStatus.REJECTED, reason));
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
