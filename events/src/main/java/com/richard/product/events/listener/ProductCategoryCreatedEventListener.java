package com.richard.product.events.listener;

import com.richard.eventbus.annotation.EventListener;
import com.richard.product.events.ProductCategoryCreatedEvent;
import com.richard.product.events.ProductCreatedEvent;
import jakarta.inject.Singleton;

@Singleton
public class ProductCategoryCreatedEventListener {

    @EventListener
    public void on(ProductCategoryCreatedEvent event) {
        System.out.println("Got event " + event);
    }
}
