package com.richard.product.events.listener;

import com.richard.eventbus.annotation.EventListener;
import com.richard.product.events.ProductCreatedEvent;
import jakarta.inject.Singleton;

@Singleton
public class ProductCreatedEventLoggerListener {

    @EventListener
    public void on(ProductCreatedEvent event) {
        System.out.println("Logger Got event " + event);
    }
}
