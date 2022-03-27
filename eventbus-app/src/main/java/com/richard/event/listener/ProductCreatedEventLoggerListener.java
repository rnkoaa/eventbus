package com.richard.event.listener;

import com.richard.event.ProductCreatedEvent;
import com.richard.eventbus.annotation.EventListener;
import com.richard.eventbus.annotation.Subscribe;

@EventListener
public class ProductCreatedEventLoggerListener {

    @Subscribe
    void on(ProductCreatedEvent event) {
        System.out.println("Got event " + event);
    }
}
