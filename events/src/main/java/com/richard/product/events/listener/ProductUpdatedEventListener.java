package com.richard.product.events.listener;

import com.richard.eventbus.annotation.EventListener;
import com.richard.product.events.ProductUpdatedEvent;
import jakarta.inject.Singleton;

@Singleton
public class ProductUpdatedEventListener {

    @EventListener
    void on(ProductUpdatedEvent event) {
        System.out.println("Got event " + event);
    }
}
