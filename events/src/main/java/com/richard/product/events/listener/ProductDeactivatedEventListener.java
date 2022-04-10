package com.richard.product.events.listener;

import com.richard.eventbus.annotation.EventListener;
import com.richard.product.events.ProductDeactivatedEvent;
import jakarta.inject.Singleton;

@Singleton
public class ProductDeactivatedEventListener {

    @EventListener
    public void on(ProductDeactivatedEvent event) {
        System.out.println("Deactivate Product: " + event);
    }
}
