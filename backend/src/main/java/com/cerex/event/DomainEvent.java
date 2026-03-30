package com.cerex.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Base domain event published to Kafka topics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainEvent {

    private String eventId;
    private String eventType;
    private UUID aggregateId;
    private String aggregateType;
    private Instant timestamp;
    private Object payload;

    public static DomainEvent of(String eventType, String aggregateType, UUID aggregateId, Object payload) {
        return DomainEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .eventType(eventType)
            .aggregateType(aggregateType)
            .aggregateId(aggregateId)
            .timestamp(Instant.now())
            .payload(payload)
            .build();
    }
}
