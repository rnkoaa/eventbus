package com.richard.eventbus;

import com.richard.eventbus.framework.EventBus;
import com.richard.eventbus.framework.EventBusIndex;
import com.richard.eventbus.framework.EventBusRegistrar;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;

@Factory
public class EventBusProvider {

    @Singleton
    public EventBusIndex eventBusIndex(ApplicationContext applicationContext) {
        var eventBusIndex = new EventBusIndexGeneratedImpl();
        eventBusIndex.getAllEventHandlerClassInfos()
            .stream()
            .map(handlerClass -> {
                Object bean = applicationContext.getBean(handlerClass.eventListenerClass());
                return handlerClass.withEventListenerInstance(bean);
            })
            .forEach(eventBusIndex::put);
        return eventBusIndex;
    }

    @Singleton
    public EventBus eventBus(EventBusIndex eventBusIndex) {
        var eventBusRegistrar = new EventBusRegistrar(eventBusIndex);
        return eventBusRegistrar.build();
    }
}
