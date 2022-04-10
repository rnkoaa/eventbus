package com.richard.product.events;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.richard.eventbus.annotation.AggregateEvent;
import com.richard.product.events.ProductCreatedEventImpl.Builder;
import org.immutables.value.Value;

import java.util.UUID;

@Value.Immutable
@AggregateEvent
@ApiStyle
@JsonDeserialize(builder = ProductCreatedEventImpl.Builder.class)
public  interface ProductCreatedEvent extends VersionedEvent {

    UUID getProductId() ;

    String getName() ;

    String getSku() ;


    static Builder newBuilder(){
        return ProductCreatedEventImpl.builder();
    }
}

