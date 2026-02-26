package com.tchelo.event_service_ticket241.kafka.producer;

import com.tchelo.event_service_ticket241.kafka.event.SeatsDecreasedEvent;
import com.tchelo.event_service_ticket241.kafka.event.SeatsDecreaseFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishSeatsDecreasedEvent(SeatsDecreasedEvent event) {
        String topic = "seats-decreased";

        log.info("Publication événement Kafka : {} → Topic: {}", event.getKafkaEventId(), topic);

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(topic, event.getOrderId(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Événement publié avec succès : {} → Partition: {}, Offset: {}",
                        event.getKafkaEventId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Échec publication événement : {}", event.getKafkaEventId(), ex);
            }
        });
    }

    @Override
    public void publishSeatsDecreaseFailedEvent(SeatsDecreaseFailedEvent event) {
        String topic = "seats-decrease-failed";

        log.info("Publication événement Kafka : {} → Topic: {}", event.getKafkaEventId(), topic);

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(topic, event.getOrderId(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Événement publié avec succès → Topic: {}, Partition: {}, Offset: {}",
                        topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Échec publication événement → Topic: {}", topic, ex);
            }
        });
    }
}
