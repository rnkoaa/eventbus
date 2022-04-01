package com.richard.eventbus.framework;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    List<EventHandlerClassInfo> findHandler(Class<?> clzz) {
        List<EventHandlerClassInfo> eventHandlers = handlers.getOrDefault(clzz, new ArrayList<>());
        if (eventHandlers.size() > 0) {
            return eventHandlers;
        }

        // potentially, this class implements an interface that is handled.
        record InterfaceHandler(Class<?> interfaceClass, List<EventHandlerClassInfo> eventHandlerClassInfos) {}

        // we only care about the immediate interfaces of classes. This prevents deep hierachical code inheritance
        Class<?>[] interfaces = clzz.getInterfaces();
        List<InterfaceHandler> interfaceHandlers = Stream.of(interfaces)
            .map(clzzInterface -> new InterfaceHandler(clzzInterface, handlers.get(clzzInterface)))
            .filter(interfaceHandler -> interfaceHandler.eventHandlerClassInfos != null)
            .toList();

        if (interfaceHandlers.size() > 0) {
            for (InterfaceHandler interfaceHandler : interfaceHandlers) {
                handlers.putIfAbsent(interfaceHandler.interfaceClass, interfaceHandler.eventHandlerClassInfos);
            }

            return interfaceHandlers.stream()
                .flatMap(it -> it.eventHandlerClassInfos.stream())
                .toList();
        }

        Class<?> superclass = clzz.getSuperclass();
        if (superclass != Object.class) {
            List<EventHandlerClassInfo> eventHandlerClassInfos = handlers.getOrDefault(superclass, new ArrayList<>());
            if (eventHandlerClassInfos.size() > 0) {
                handlers.putIfAbsent(superclass, eventHandlerClassInfos);
                return eventHandlerClassInfos;
            }
        }

        // look for the immediate super class to see if it is available to use

        return Collections.emptyList();
    }

    @Override
    public void publish(Object event) {
        if (event == null) {
            throw new EventListenerException("unable to register a listener. should not be null");
        }

        if (logListener != null && logListener.isEnabled()) {
            logListener.onEvent(event);
        }

        List<EventHandlerClassInfo> eventHandlers = findHandler(event.getClass());
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
