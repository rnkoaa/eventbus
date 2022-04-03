package com.richard.product.events;

import com.richard.eventbus.annotation.EventSourcingListener;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Product {

    private UUID id;
    private String name;
    private String sku;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean active;

    @EventSourcingListener
    public void on(ProductCreatedEvent productCreatedEvent) {
        this.id = productCreatedEvent.getProductId();
        this.name = productCreatedEvent.getName();
        this.sku = productCreatedEvent.getSku();
        this.active = true;
        this.createdAt = productCreatedEvent.getCreatedAt();
    }

    @EventSourcingListener
    public void on(ProductUpdatedEvent productUpdatedEvent) {
        this.name = productUpdatedEvent.getName();
        this.sku = productUpdatedEvent.getSku();
        this.updatedAt = productUpdatedEvent.getCreatedAt();
    }

    @EventSourcingListener
    public void on(ProductDeactivatedEvent productDeactivatedEvent) {
        this.active = false;
        this.updatedAt = productDeactivatedEvent.getCreatedAt();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return id.equals(product.id) && name.equals(product.name) && sku.equals(product.sku);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, sku);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSku() {
        return sku;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean isActive() {
        return active;
    }
}
