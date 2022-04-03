package com.richard.eventbus;

import com.richard.eventbus.framework.EventBus;
import com.richard.eventbus.framework.EventBusIndex;
import com.richard.eventbus.framework.EventBusRegistrar;
import com.richard.product.events.Product;
import com.richard.product.events.ProductCreatedEvent;
import com.richard.product.events.ProductDeactivatedEvent;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.Micronaut;
import jakarta.inject.Singleton;

import java.util.UUID;

@Singleton
public class Application implements ApplicationEventListener<StartupEvent> {

    private final ApplicationContext applicationContext;
    private final EventBus eventBus;

    public Application(ApplicationContext applicationContext, EventBus eventBus) {
        this.applicationContext = applicationContext;
        this.eventBus = eventBus;
    }

    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }

    @Override
    public void onApplicationEvent(StartupEvent event) {
        eventBus.registerDeadLetterListener(object ->
            System.out.println("Got a Dead letter event of type " + object.getClass() + ", Value: " + object)
        );
//
        eventBus.publish(ProductCreatedEvent.newBuilder()
            .aggregateId(UUID.randomUUID())
            .id(UUID.randomUUID())
            .productId(UUID.randomUUID())
            .aggregateName(Product.class.getSimpleName())
            .name("Product 1")
            .sku("Product 2")
            .build());
        eventBus.publish(ProductDeactivatedEvent.newBuilder()
            .aggregateId(UUID.randomUUID())
            .id(UUID.randomUUID())
            .aggregateName(Product.class.getSimpleName())
            .build());

    }
}
