package com.richard.product.events;

import java.util.UUID;

public record ProductUpdatedEvent(UUID productId, String name, String sku) {

}
