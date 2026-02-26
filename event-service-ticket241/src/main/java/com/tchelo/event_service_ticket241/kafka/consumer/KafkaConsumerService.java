package com.tchelo.event_service_ticket241.kafka.consumer;


import com.tchelo.event_service_ticket241.kafka.event.*;
import com.tchelo.event_service_ticket241.kafka.producer.KafkaProducerService;
import com.tchelo.event_service_ticket241.service.TicketCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
public class KafkaConsumerService {

    private final TicketCategoryService ticketCategoryService;
    private final KafkaProducerService kafkaProducerService;

    // ÉCOUTE : Ticket Réservé depuis mon ticket service
    @KafkaListener(
            topics = "ticket-reserved",
            groupId = "event-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleTicketReserved(
            TicketReservedEvent event,
            Acknowledgment acknowledgment
    ) {
        log.info("Événement reçu : ticket-reserved pour order {}", event.getOrderId());

        try {
            // Réserver les places dans la catégorie
            ticketCategoryService.reserveSeats(
                    event.getTicketCategoryId(),
                    event.getQuantity()
            );

            log.info("{} places réservées pour catégorie {}",
                    event.getQuantity(), event.getTicketCategoryId());

            // Publier événement de confirmation
            SeatsDecreasedEvent confirmationEvent = SeatsDecreasedEvent.builder()
                    .eventId(event.getEventIdLong())
                    .eventUuid(event.getEventUuid())
                    .categoryId(event.getTicketCategoryId())
                    .quantity(event.getQuantity())
                    .ticketIds(event.getTicketIds())
                    .orderId(event.getOrderId())
                    .success(true)
                    .message("Places réservées avec succès")
                    .processedAt(LocalDateTime.now())
                    .kafkaEventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .build();

            kafkaProducerService.publishSeatsDecreasedEvent(confirmationEvent);

            // Acknowledge le message Kafka
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Erreur lors de la réservation : {}", e.getMessage(), e);

            // Publier événement d'échec
            SeatsDecreaseFailedEvent failureEvent = SeatsDecreaseFailedEvent.builder()
                    .eventId(event.getEventIdLong())
                    .categoryId(event.getTicketCategoryId())
                    .quantity(event.getQuantity())
                    .ticketIds(event.getTicketIds())
                    .orderId(event.getOrderId())
                    .failureReason(e.getMessage())
                    .failedAt(LocalDateTime.now())
                    .kafkaEventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now())
                    .build();

            kafkaProducerService.publishSeatsDecreaseFailedEvent(failureEvent);

            // Acknowledge quand même pour éviter de rejouer
            acknowledgment.acknowledge();
        }
    }


    // ÉCOUTE : Ticket Annulé depuis mon ticket service

    @KafkaListener(
            topics = "ticket-cancelled",
            groupId = "event-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleTicketCancelled(
            TicketCancelledEvent event,
            Acknowledgment acknowledgment
    ) {
        log.info("Événement reçu : ticket-cancelled pour ticket {}", event.getTicketNumber());

        try {
            // Libérer les places dans la catégorie
            ticketCategoryService.increaseAvailableSeats(
                    event.getCategoryId(),
                    event.getQuantity()
            );

            log.info("{} places libérées pour catégorie {}",
                    event.getQuantity(), event.getCategoryId());

            // Acknowledge le message Kafka
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Erreur lors de l'annulation : {}", e.getMessage(), e);
            acknowledgment.acknowledge();
        }
    }


    // ÉCOUTE : Réservation Expirée toujours depuis mon servive de ticket

    @KafkaListener(
            topics = "reservation-expired",
            groupId = "event-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleReservationExpired(
            ReservationExpiredEvent event,
            Acknowledgment acknowledgment
    ) {
        log.info("Événement reçu : reservation-expired pour ticket {}", event.getTicketNumber());

        try {
            // Annuler la réservation (libérer les places)
            ticketCategoryService.cancelReservation(
                    event.getCategoryId(),
                    event.getQuantity()
            );

            log.info("Réservation expirée : {} places libérées pour catégorie {}",
                    event.getQuantity(), event.getCategoryId());

            // Acknowledge le message Kafka
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Erreur lors du traitement de l'expiration : {}", e.getMessage(), e);
            acknowledgment.acknowledge();
        }
    }
}
