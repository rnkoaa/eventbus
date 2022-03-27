package com.richard.event;

import java.util.UUID;

public record ProductUpdatedEvent(UUID productId, String name, String sku) {

}
