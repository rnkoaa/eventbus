package com.richard.product.events;

import java.util.UUID;

public record ProductCreatedEvent(UUID productId, String name, String sku) {

}
