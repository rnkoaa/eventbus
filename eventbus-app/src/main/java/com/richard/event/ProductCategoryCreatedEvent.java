package com.richard.event;

import java.util.UUID;

public record ProductCategoryCreatedEvent(
    UUID productId,
    UUID categoryId,
    String categoryName
) {

}
