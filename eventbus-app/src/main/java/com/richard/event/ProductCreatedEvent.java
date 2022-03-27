package com.richard.event;

import java.util.UUID;

public record ProductCreatedEvent(UUID productId, String name, String sku) {

}
