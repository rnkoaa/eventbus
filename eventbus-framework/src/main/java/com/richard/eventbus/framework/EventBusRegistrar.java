package com.richard.eventbus.framework;

import com.richard.eventbus.annotation.EventListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

public class EventBusRegistrar {

    EventBus eventBus;

    public EventBusRegistrar(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void register(EventHandlerClassInfo eventHandlerClassInfo) {
        Method[] declaredMethods = eventHandlerClassInfo.eventListenerClass().getDeclaredMethods();
        Stream.of(declaredMethods)
            .filter(method -> method.isAnnotationPresent(EventListener.class))
            .filter(method -> method.getParameterCount() == 1)
            .map(eventHandlerClassInfo::withHandlerMethod)
            .forEach(eventHandlerClassWithMethod -> eventBus.register(eventHandlerClassWithMethod));
    }

//    @PostConstruct
    public List<EventHandlerClassInfo> loadEventListeners() {
        String path = "META-INF/events/eventbusHandlerReference";
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
