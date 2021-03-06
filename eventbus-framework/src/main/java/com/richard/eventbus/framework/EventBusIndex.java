package com.richard.eventbus.framework;

import java.util.Collection;

public interface EventBusIndex {
    Collection<EventHandlerClassInfo> getEventHandlerClass(Class<?> eventClass);

    Collection<EventHandlerClassInfo> getAllEventHandlerClassInfos();

    void put(EventHandlerClassInfo eventClassInfo);

    default String metrics() {
        return "undefined";
    }
}
