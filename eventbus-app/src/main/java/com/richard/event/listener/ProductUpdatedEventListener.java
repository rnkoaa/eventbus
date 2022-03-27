package com.richard.event.listener;

import com.richard.event.ProductCreatedEvent;
import com.richard.event.ProductUpdatedEvent;
import com.richard.eventbus.annotation.EventListener;
import com.richard.eventbus.annotation.Subscribe;

@EventListener
public class ProductUpdatedEventListener {

    @Subscribe
    void on(ProductUpdatedEvent event) {
        System.out.println("Got event " + event);
    }
}
