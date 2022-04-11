package com.richard.product;


import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@MicronautTest
class ProductServiceTest {

    @Inject
    private ProductService productService;

    @Test
    void serviceInjected() {
        assertThat(productService).isNotNull();
    }

    @Test
    void insertOneRecord() {
        var productResult = productService.create(new Product());
        assertThat(productResult.getId()).isGreaterThan(0);
    }

}