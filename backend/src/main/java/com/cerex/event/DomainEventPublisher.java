package com.cerex.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Publishes domain events to Kafka topics asynchronously.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DomainEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Async
    public void publish(String topic, DomainEvent event) {
        try {
            kafkaTemplate.send(topic, event.getAggregateId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish event {} to topic {}: {}",
                            event.getEventId(), topic, ex.getMessage());
                    } else {
                        log.debug("Event published: {} → {} (offset={})",
                            event.getEventType(), topic,
                            result.getRecordMetadata().offset());
                    }
                });
        } catch (Exception e) {
            log.error("Error publishing event to {}: {}", topic, e.getMessage());
        }
    }
}
