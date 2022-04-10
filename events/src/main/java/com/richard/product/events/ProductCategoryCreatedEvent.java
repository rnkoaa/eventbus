package com.richard.product.events;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.richard.eventbus.annotation.AggregateEvent;
import com.richard.product.events.ProductCategoryCreatedEventImpl.Builder;
import org.immutables.value.Value;

import java.util.UUID;

@Value.Immutable
@AggregateEvent
@ApiStyle
@JsonDeserialize(builder = ProductCategoryCreatedEventImpl.Builder.class)
public interface ProductCategoryCreatedEvent extends VersionedEvent {

    UUID getProductId();

    UUID getCategoryId();

    String getName();

    static Builder newBuilder() {
        return ProductCategoryCreatedEventImpl.builder();
    }

}
