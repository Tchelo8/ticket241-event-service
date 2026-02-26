package com.tchelo.event_service_ticket241.kafka.producer;


import com.tchelo.event_service_ticket241.kafka.event.SeatsDecreasedEvent;
import com.tchelo.event_service_ticket241.kafka.event.SeatsDecreaseFailedEvent;

public interface KafkaProducerService {

    // Publie un événement "Places Décrémentées" pour les autres services qui en auront besoin avant d'effectuer certaines taches
    void publishSeatsDecreasedEvent(SeatsDecreasedEvent event);

    // Publie un événement "Échec Décrémentation Places"
    void publishSeatsDecreaseFailedEvent(SeatsDecreaseFailedEvent event);
}
