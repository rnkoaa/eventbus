package com.richard.eventbus;

import io.micronaut.core.annotation.Introspected;

import java.util.Objects;

@Introspected
public class ProductCreatedRequest {
    private String name;
    private String sku;

    public ProductCreatedRequest() {
    }

    public ProductCreatedRequest(String name, String sku) {
        this.name = name;
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductCreatedRequest that = (ProductCreatedRequest) o;
        return Objects.equals(name, that.name) && Objects.equals(sku, that.sku);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, sku);
    }

    @Override
    public String toString() {
        return "ProductCreatedRequest{" +
                "name='" + name + '\'' +
                ", sku='" + sku + '\'' +
                '}';
    }
}
