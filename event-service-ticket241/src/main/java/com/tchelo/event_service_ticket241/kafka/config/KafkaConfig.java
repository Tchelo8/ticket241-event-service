package com.tchelo.event_service_ticket241.kafka.config;


import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    // TOPICS PUBLIÉS PAR EVENT SERVICE

    @Bean
    public NewTopic seatsDecreasedTopic() {
        return TopicBuilder.name("seats-decreased")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic seatsDecreaseFailedTopic() {
        return TopicBuilder.name("seats-decrease-failed")
                .partitions(3)
                .replicas(1)
                .build();
    }

    // Note: Les topics "ticket-reserved", "ticket-cancelled", "reservation-expired"
    // seront créés automatiquement par Ticket Service
}
