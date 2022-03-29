package com.richard.eventbus;

import com.richard.eventbus.framework.EventBus;
import com.richard.eventbus.framework.EventBusIndex;
import com.richard.eventbus.framework.EventBusRegistrar;
import com.richard.eventbus.framework.EventHandlerClassInfo;
import com.richard.eventbus.framework.InMemoryEventBus;
import com.richard.product.events.ProductCreatedEvent;
import com.richard.product.events.listener.ProductCreatedEventListener;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.Micronaut;
import jakarta.inject.Singleton;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

@Singleton
public class Application implements ApplicationEventListener<StartupEvent> {

    private final ApplicationContext applicationContext;

    public Application(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }

    @Override
    public void onApplicationEvent(StartupEvent event) {
        EventBusIndex eventBusIndex = new EventBusIndexImpl();
        eventBusIndex.getEventHandlers()
                .values()
                .stream()
                .map(entry -> {
                    Object bean = applicationContext.getBean(entry.eventListenerClass());
                     return entry.withEventListenerInstance(bean);

                })
                .forEach(entry -> {
                   eventBusIndex.put(entry);
                });
//        EventBus eventBus = InMemoryEventBus.getInstance();
//        EventBusRegistrar eventBusRegistrar = new EventBusRegistrar(eventBus);
//        var eventListeners = eventBusRegistrar.loadEventListeners();
//        eventListeners.stream()
//            .map(eventHandlerClassInfo -> {
//                Object bean = applicationContext.getBean(eventHandlerClassInfo.eventListenerClass());
//                return eventHandlerClassInfo.withEventListenerInstance(bean);
//            })
//            .forEach(eventBusRegistrar::register);
//
//        System.out.println("Known Events: " + eventBus.getSubscribers().size());
//        eventBus.publish(new ProductCreatedEvent(UUID.randomUUID(), "Product 1", "Sku"));

//        try {
//            ProductCreatedEventListener productCreatedEventListener = applicationContext.getBean(ProductCreatedEventListener.class);
//            Method on = ProductCreatedEventListener.class.getDeclaredMethod("on", ProductCreatedEvent.class);
//            on.invoke(productCreatedEventListener, new ProductCreatedEvent(UUID.randomUUID(), "Product 1", "sku-1"));
//
//        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
//            throw new RuntimeException(e);
//        }
    }
}
