package com.richard.product.events;

import java.util.UUID;

public record ProductCategoryCreatedEvent(
    UUID productId,
    UUID categoryId,
    String categoryName
) {

}
