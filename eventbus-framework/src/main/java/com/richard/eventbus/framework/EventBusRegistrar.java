package com.richard.eventbus.framework;

import com.richard.eventbus.annotation.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class EventBusRegistrar {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventBusRegistrar.class);

    EventBusIndex eventBusIndex;

    public EventBusRegistrar(EventBusIndex eventBusIndex) {
        this.eventBusIndex = eventBusIndex;
    }

    public EventBus build() {
        EventBus eventBus = InMemoryEventBus.getInstance();

        eventBusIndex.getAllEventHandlerClassInfos()
                .stream()
                .map(eventHandlerClassInfo -> findHandlerMethod(eventHandlerClassInfo)
                        .map(eventHandlerClassInfo::withHandlerMethod))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(eventBus::register);
        return eventBus;
    }

    private static Optional<Method> findHandlerMethod(EventHandlerClassInfo eventHandlerClassInfo) {
        Class<?> aClass = eventHandlerClassInfo.eventListenerClass();
        try {
            Method method = aClass.getMethod(eventHandlerClassInfo.methodName(), eventHandlerClassInfo.eventClass());
            return Optional.of(method);
        } catch (NoSuchMethodException e) {
            LOGGER.info("Method {} not found on Class {}, will attempt a brute force.",
                    eventHandlerClassInfo.methodName(),
                    eventHandlerClassInfo.eventClass().getName());
        }

        Method[] declaredMethods = eventHandlerClassInfo.eventListenerClass().getDeclaredMethods();
        return Stream.of(declaredMethods)
                .filter(method -> method.isAnnotationPresent(EventListener.class))
                .filter(method -> method.getParameterCount() == 1)
                .findFirst();
    }

    //    @PostConstruct
    public List<EventHandlerClassInfo> loadEventListeners() {
        String path = "META-INF/event-bus/com.richard.eventbus.EventIndexFile";
        List<String> eventListenerDetails = loadEventListenerInfoFile(path);
        return eventListenerDetails
                .stream()
                .map(line -> line.split(","))
                .filter(line -> line.length == 3)
                .map(classInfoPaths -> {
                    String eventPath = classInfoPaths[0];
                    String eventListener = classInfoPaths[1];
                    String methodName = classInfoPaths[2];

                    Class<?> eventClass = null;
                    Class<?> eventListenerClass = null;
                    try {
                        eventClass = Class.forName(eventPath);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    try {
                        eventListenerClass = Class.forName(eventListener);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    return new EventHandlerClassInfo(eventClass, eventListenerClass);
                })
                .filter(EventHandlerClassInfo::valid)
                .toList();
    }

    private List<String> loadEventListenerInfoFile(String path) {
        InputStream resourceAsStream = EventBus.class.getClassLoader().getResourceAsStream(path);
        if (resourceAsStream == null) {
            throw new IllegalArgumentException("Event Info File " + path + " does not exist");
        }

        InputStreamReader strrd = new InputStreamReader(resourceAsStream);
        return new BufferedReader(strrd)
                .lines()
                .toList();
    }
}
