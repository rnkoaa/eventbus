package com.richard.eventbus.framework;

import java.util.Map;
import java.util.Optional;

public interface EventBusIndex {
    Optional<EventHandlerClassInfo> getEventHandlerClass(Class<?> eventClass);

    Map<Class<?>, EventHandlerClassInfo> getEventHandlers();

    void put(EventHandlerClassInfo eventClassInfo);
}
