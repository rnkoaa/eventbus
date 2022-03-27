package com.richard.event.saga;

import com.richard.event.ProductCategoryCreatedEvent;
import com.richard.event.ProductCreatedEvent;
import com.richard.eventbus.annotation.Saga;
import com.richard.eventbus.annotation.StartSaga;
import com.richard.eventbus.annotation.Subscribe;

@Saga
public class ProductSaga {

    @StartSaga
    ProductSaga(ProductCreatedEvent event) {

    }

    @Subscribe
    void on(ProductCategoryCreatedEvent event) {

    }
}
