package com.richard.product.events;

import com.richard.eventbus.annotation.AggregateEvent;
import com.richard.product.events.ProductUpdatedEventImpl.Builder;
import org.immutables.value.Value;

import java.util.UUID;

@Value.Immutable
@AggregateEvent
@ApiStyle
public  interface ProductUpdatedEvent extends VersionedEvent {

    UUID getProductId() ;

    String getName() ;

    String getSku() ;

    static Builder newBuilder(){
        return ProductUpdatedEventImpl.builder();
    }

}
