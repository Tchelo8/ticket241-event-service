package com.tchelo.event_service_ticket241.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// Clé métier des messages Kafka déjà appliqués (ex: "ticket-paid:<orderId>", "release:<ticketId>").
// Kafka livre "au moins une fois" : sans cette trace, un message rejoué libérerait ou vendrait
// les mêmes places deux fois.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "processed_kafka_messages")
public class ProcessedKafkaMessage {

    @Id
    @Column(name = "message_key", length = 120)
    private String messageKey;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;
}
