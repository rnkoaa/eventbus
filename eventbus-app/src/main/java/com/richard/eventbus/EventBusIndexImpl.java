package com.richard.eventbus;

import com.richard.eventbus.framework.EventBusIndex;
import com.richard.eventbus.framework.EventHandlerClassInfo;
import com.richard.product.events.ProductCreatedEvent;
import com.richard.product.events.ProductUpdatedEvent;
import com.richard.product.events.listener.ProductCreatedEventListener;
import com.richard.product.events.listener.ProductUpdatedEventListener;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class EventBusIndexImpl implements EventBusIndex {
    private static final Map<Class<?>, EventHandlerClassInfo> eventHandlers = new ConcurrentHashMap<>();

    static {
        eventHandlers.put(ProductCreatedEvent.class, new EventHandlerClassInfo(
                ProductCreatedEvent.class, ProductCreatedEventListener.class, "on"
        ));
        eventHandlers.put(ProductUpdatedEvent.class, new EventHandlerClassInfo(
                ProductUpdatedEvent.class, ProductUpdatedEventListener.class, "on"
        ));
    }

    @Override
    public Optional<EventHandlerClassInfo> getEventHandlerClass(Class<?> eventClass) {
        return Optional.ofNullable(eventHandlers.get(eventClass));
    }

    @Override
    public Map<Class<?>, EventHandlerClassInfo> getEventHandlers() {
        return Map.copyOf(eventHandlers);
    }

    @Override
    public void put(EventHandlerClassInfo eventClassInfo) {
        Objects.requireNonNull(eventClassInfo, "Event class info cannot be null");
        eventHandlers.put(eventClassInfo.eventClass(), eventClassInfo);
    }
}
