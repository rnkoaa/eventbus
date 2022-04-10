package com.richard.eventbus.framework;

import java.util.Set;

public interface EventBus {

    void registerLogger(LogListener logListener);

    void registerDeadLetterListener(DeadLetterEventListener deadLetterEventListener);

    void register(EventHandlerClassInfo subscriber);

    void publish(Object event);

    Set<EventHandlerClassInfo> getSubscribers();

    void unRegister(EventHandlerClassInfo subscriber);

    /**
     * This stops all background threads and shuts down any thread pools used for background processing
     */
    default void stop() {
        publish(new PoisonPill());
    }
}
