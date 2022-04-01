package com.richard.product.events;

import com.richard.eventbus.annotation.AggregateEvent;
import com.richard.product.events.ProductDeactivatedEventImpl.Builder;
import org.immutables.value.Value;

@Value.Immutable
@AggregateEvent
@ApiStyle
public abstract class ProductDeactivatedEvent implements VersionedEvent{
    public static Builder newBuilder(){
        return ProductDeactivatedEventImpl.builder();
    }
}
