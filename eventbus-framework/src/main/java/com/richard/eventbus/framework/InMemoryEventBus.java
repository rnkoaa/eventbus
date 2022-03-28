package com.richard.eventbus.framework;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryEventBus implements EventBus {

    private DeadLetterEventListener deadLetterEventListener;
    private LogListener logListener;
    private static volatile InMemoryEventBus instance;
    private static final Map<Class<?>, List<EventHandlerClassInfo>> handlers = new ConcurrentHashMap<>();

    public void registerLogger(LogListener logListener) {
        this.logListener = logListener;
    }

    public void registerDeadLetterListener(DeadLetterEventListener deadLetterEventListener) {
        this.deadLetterEventListener = deadLetterEventListener;
    }

    public static InMemoryEventBus getInstance() {
        if (instance == null) {
            synchronized (InMemoryEventBus.class) {
                if (instance == null) {
                    instance = new InMemoryEventBus();
                }
            }
        }
        return instance;
    }

    @Override
    public void register(EventHandlerClassInfo eventHandlerClassInfo) {
        List<EventHandlerClassInfo> eventHandlers = handlers.getOrDefault(
            eventHandlerClassInfo.eventClass(),
            new ArrayList<>()
        );

        eventHandlers.add(eventHandlerClassInfo);
        handlers.put(eventHandlerClassInfo.eventClass(), eventHandlers);
    }

    @Override
    public void publish(Object event) {
        if (event == null) {
            throw new EventListenerException("unable to register a listener. should not be null");
        }

        if (logListener != null && logListener.isEnabled()) {
            logListener.onEvent(event);
        }
        List<EventHandlerClassInfo> eventHandlers = handlers.getOrDefault(event.getClass(), new ArrayList<>());
        if (eventHandlers.size() == 0 && deadLetterEventListener != null) {
            deadLetterEventListener.onEvent(event);
            return;
        }

        for (EventHandlerClassInfo eventHandler : eventHandlers) {
            Method method = eventHandler.handlerMethod();
            Object handlerInstance = eventHandler.eventListenerInstance();
            try {
                method.invoke(handlerInstance, event);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new EventBusException(
                    String.format("Failed to dispatch event %s@%d to listener %s@%d",
                        event.getClass().getName(),
                        System.identityHashCode(event),
                        handlerInstance.getClass().getName(),
                        System.identityHashCode(handlerInstance)),
                    e);
            }
        }
    }

    @Override
    public Set<EventHandlerClassInfo> getSubscribers() {
        return handlers.values()
            .stream()
            .filter(it -> it != null && it.size() > 0)
            .flatMap(Collection::stream).collect(Collectors.toSet());
    }

    @Override
    public void unRegister(EventHandlerClassInfo subscriber) {

    }
}
