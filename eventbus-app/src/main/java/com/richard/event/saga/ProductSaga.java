package com.richard.event.saga;

import com.richard.eventbus.annotation.Saga;
import com.richard.eventbus.annotation.SagaEventListener;
import com.richard.eventbus.annotation.StartSaga;
import com.richard.product.events.ProductCategoryCreatedEvent;
import com.richard.product.events.ProductCreatedEvent;

@Saga
public class ProductSaga {

    @StartSaga
    void on(ProductCreatedEvent event) {

    }

    @SagaEventListener
    void on(ProductCategoryCreatedEvent event) {

    }
}
