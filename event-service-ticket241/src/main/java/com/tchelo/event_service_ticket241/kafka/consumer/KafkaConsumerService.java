package com.tchelo.event_service_ticket241.kafka.consumer;


import com.tchelo.event_service_ticket241.kafka.event.*;
import com.tchelo.event_service_ticket241.kafka.producer.KafkaProducerService;
import com.tchelo.event_service_ticket241.service.SeatReservationService;
import com.tchelo.event_service_ticket241.service.SeatReservationService.ReservationOutcome;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

// Côté event-service de la saga de réservation. Chaque message est appliqué dans une transaction
// (SeatReservationService), puis la réponse est publiée APRÈS le commit : ticket-service ne peut donc
// jamais recevoir "places réservées" pour une réservation annulée en base.
// Une exception remonte au conteneur, qui rejoue le message puis l'envoie dans <topic>.DLT
// (KafkaListenerConfig) ; les traitements étant idempotents, rejouer est sans risque.
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
public class KafkaConsumerService {

    private final SeatReservationService seatReservationService;
    private final KafkaProducerService kafkaProducerService;

    // ÉCOUTE : billets créés dans ticket-service → réserver les places de la commande
    @KafkaListener(topics = "ticket-reserved", groupId = "event-service-group")
    public void handleTicketReserved(TicketReservedEvent event) {
        log.info("Événement reçu : ticket-reserved pour la commande {}", event.getOrderId());

        ReservationOutcome outcome = seatReservationService.reserve(
                event.getOrderId(), event.getTicketCategoryId(), event.getQuantity());

        if (outcome.accepted()) {
            kafkaProducerService.publishSeatsDecreasedEvent(SeatsDecreasedEvent.builder()
                    .eventId(outcome.eventId())
                    .eventUuid(event.getEventUuid())
                    .categoryId(event.getTicketCategoryId())
                    .quantity(event.getQuantity())
                    .ticketIds(event.getTicketIds())
                    .orderId(event.getOrderId())
                    .success(true)
                    .message(outcome.message())
                    .processedAt(LocalDateTime.now())
                    .kafkaEventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .build());
        } else {
            kafkaProducerService.publishSeatsDecreaseFailedEvent(SeatsDecreaseFailedEvent.builder()
                    .eventId(outcome.eventId() != null ? outcome.eventId() : event.getEventIdLong())
                    .categoryId(event.getTicketCategoryId())
                    .quantity(event.getQuantity())
                    .ticketIds(event.getTicketIds())
                    .orderId(event.getOrderId())
                    .failureReason(outcome.message())
                    .failedAt(LocalDateTime.now())
                    .kafkaEventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    // ÉCOUTE : paiement confirmé → les places réservées de la commande deviennent vendues
    @KafkaListener(topics = "ticket-paid", groupId = "event-service-group")
    public void handleTicketPaid(TicketPaidEvent event) {
        log.info("Événement reçu : ticket-paid pour la commande {}", event.getOrderId());
        seatReservationService.confirmPaidOrder(event.getOrderId(), event.getQuantity());
    }

    // ÉCOUTE : billet annulé → libère une place réservée ou vendue selon son statut d'avant
    @KafkaListener(topics = "ticket-cancelled", groupId = "event-service-group")
    public void handleTicketCancelled(TicketCancelledEvent event) {
        log.info("Événement reçu : ticket-cancelled pour le billet {} (statut précédent : {})",
                event.getTicketNumber(), event.getPreviousStatus());

        if ("PAID".equals(event.getPreviousStatus())) {
            seatReservationService.releaseSoldTicket(event.getOrderId(), event.getTicketId());
        } else {
            seatReservationService.releaseReservedTicket(event.getOrderId(), event.getTicketId());
        }
    }

    // ÉCOUTE : réservation expirée (non payée à temps, ou jamais confirmée) → libère sa place
    @KafkaListener(topics = "reservation-expired", groupId = "event-service-group")
    public void handleReservationExpired(ReservationExpiredEvent event) {
        log.info("Événement reçu : reservation-expired pour le billet {}", event.getTicketNumber());
        seatReservationService.releaseReservedTicket(event.getOrderId(), event.getTicketId());
    }
}
