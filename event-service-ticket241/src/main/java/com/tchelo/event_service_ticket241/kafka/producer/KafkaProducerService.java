package com.tchelo.event_service_ticket241.kafka.producer;


import com.tchelo.event_service_ticket241.kafka.event.SeatsDecreasedEvent;
import com.tchelo.event_service_ticket241.kafka.event.SeatsDecreaseFailedEvent;

// Envois synchrones : ils lèvent une exception si le broker ne confirme pas, ce qui fait rejouer le
// message entrant. Le traitement étant idempotent, la réponse est simplement republiée.
public interface KafkaProducerService {

    // Places réservées pour la commande
    void publishSeatsDecreasedEvent(SeatsDecreasedEvent event);

    // Réservation refusée pour la commande
    void publishSeatsDecreaseFailedEvent(SeatsDecreaseFailedEvent event);
}
