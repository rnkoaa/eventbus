package com.richard.eventbus;

import com.richard.eventbus.framework.EventBus;
import com.richard.product.events.Product;
import com.richard.product.events.ProductCreatedEvent;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Status;

import java.util.UUID;

@Controller("/product")
public class ProductResource {

    private final EventBus eventBus;

    ProductResource(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Post
    @Status(HttpStatus.CREATED)
    public void createProduct(@Body ProductCreatedRequest productCreatedRequest) {
        var aggregateId = UUID.randomUUID();
        var productCreatedEvent = ProductCreatedEvent.newBuilder()
                .id(UUID.randomUUID())
                .aggregateId(aggregateId)
                .aggregateName("%s:%s".formatted(Product.class.getSimpleName(), aggregateId))
                .name(productCreatedRequest.getName())
                .sku(productCreatedRequest.getSku())
                .productId(aggregateId)
                .build();
        eventBus.publish(productCreatedEvent);

    }
}
