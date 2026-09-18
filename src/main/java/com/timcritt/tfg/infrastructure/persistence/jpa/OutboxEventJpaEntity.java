package com.timcritt.tfg.infrastructure.persistence.jpa;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnTransformer;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

@Entity
@Table(name = "outbox_event")
public class OutboxEventJpaEntity {

    @Id
    private UUID id;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 100)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(
            name = "payload",
            nullable = false,
            columnDefinition = "jsonb"
    )
    @ColumnTransformer(write = "?::jsonb")
    private String payload;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected OutboxEventJpaEntity() {
    }

    public OutboxEventJpaEntity(
            UUID id,
            String aggregateType,
            String aggregateId,
            String eventType,
            String payload,
            Instant createdAt
    ) {
        this.id = id;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.createdAt = createdAt;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getEventType() {
        return eventType;
    }

    public @Nullable Object getId() {
        return id;
    }

    public @Nullable Object getCreatedAt() {
        return createdAt;
    }

    public String getPayload() {
        return payload;
    }

    // getters...
}