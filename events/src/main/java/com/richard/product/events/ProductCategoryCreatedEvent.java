package com.richard.product.events;

import com.richard.eventbus.annotation.AggregateEvent;
import org.immutables.value.Value;

import java.util.UUID;

@Value.Immutable
@AggregateEvent
@ApiStyle
//@JsonDeserialize(builder = ProductCategoryCreatedEvent.Builder.class)
public interface ProductCategoryCreatedEvent extends VersionedEvent {

    UUID getProductId();

    UUID getCategoryId();

    String getName();


}
