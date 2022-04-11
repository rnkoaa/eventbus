package com.richard.product;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@MicronautTest
class CategoryServiceTest {

    @Inject
    private CategoryService categoryService;

    @Test
    void contextLoads() {
        assertThat(categoryService).isNotNull();
    }

    @Test
    void saveOneCategory() {
        var category = new Category();
        category.setName("Menswear");

        category = categoryService.save(category);
        assertThat(category).isNotNull();
        assertThat(category.getId()).isGreaterThan(0);
    }

}