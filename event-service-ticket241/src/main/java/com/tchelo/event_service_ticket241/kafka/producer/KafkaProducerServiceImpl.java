package com.tchelo.event_service_ticket241.kafka.producer;

import com.tchelo.event_service_ticket241.kafka.event.SeatsDecreasedEvent;
import com.tchelo.event_service_ticket241.kafka.event.SeatsDecreaseFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true")
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private static final long SEND_TIMEOUT_SECONDS = 10;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishSeatsDecreasedEvent(SeatsDecreasedEvent event) {
        send("seats-decreased", event.getOrderId(), event);
    }

    @Override
    public void publishSeatsDecreaseFailedEvent(SeatsDecreaseFailedEvent event) {
        send("seats-decrease-failed", event.getOrderId(), event);
    }

    // Clé = orderId : tous les messages d'une commande vont dans la même partition, donc sont lus dans l'ordre
    private void send(String topic, String orderId, Object payload) {
        try {
            SendResult<String, Object> result = kafkaTemplate.send(topic, orderId, payload)
                    .get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            log.info("Événement publié → topic {}, commande {}, partition {}, offset {}",
                    topic, orderId, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Publication interrompue sur " + topic, e);
        } catch (Exception e) {
            throw new IllegalStateException("Échec de publication sur " + topic + " pour la commande " + orderId, e);
        }
    }
}
