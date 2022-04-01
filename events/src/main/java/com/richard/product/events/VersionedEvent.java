package com.richard.product.events;

import org.immutables.value.Value;

import java.time.Instant;
import java.util.UUID;

public interface VersionedEvent {

    UUID getId();

    UUID getAggregateId();

    @Value.Default
    default String getStreamId() {
        return this.getAggregateName().contains(":")
                ? this.getAggregateName()
                : String.format("%s:%s", this.getAggregateName(), this.getAggregateId().toString());
    }

    String getAggregateName() ;

    @Value.Default
    default long getVersion() {
        return 0L;
    }

    @Value.Default
    default Instant getCreatedAt() {
        return Instant.now();
    }
}
