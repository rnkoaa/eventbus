package com.richard.product;


import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Set;

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
        var product = new Product();
        // 1
        // 85679
        // 1
        // 63-2820003-010	Puma Platform Suede Core in White
        // https://www.frankandoak.com/catalog/product/view/id/85679/
        // <p>Born in the late '60s and rocked by generations, the Suede is arguably PUMA’s most iconic sneaker. Today, we’re taking its sport-inspired swagger to new heights (literally) with the Suede Platform. A women’s-only silhouette, it boasts a thick platform sole, ridged tooling, and plenty of edge. It’s a gamechanger of a sneaker… for women who are ready to change the game.</p><ul><li>Suede upper with perf detailing at midfoot</li><li>Lace closure for a snug fit</li><li>Platform rubber outsole with ridged tooling at toe</li><li>PUMA Formstrip at lateral and medial sides</li><li>PUMA Suede callout in metallic foil at lateral side</li><li>Woven PUMA Logo Label at tongue</li></ul>	{"usd": 0.0}	{"qty": 9, "isInStock": 0}	{"base": true, "parent": true, "weight": false, "is_base": true, "is_parent": true, "frankoak_hs_code": "6403.99.60.75", "short_description": "<p>Born in the late '60s and rocked by generations, the Suede is arguably PUMA’s most iconic sneaker. Today, we’re taking its sport-inspired swagger to new heights (literally) with the Suede Platform. A women’s-only silhouette, it boasts a thick platform sole, ridged tooling, and plenty of edge. It’s a gamechanger of a sneaker… for women who are ready to change the game.</p><ul><li>Suede upper with perf detailing at midfoot</li><li>Lace closure for a snug fit</li><li>Platform rubber outsole with ridged tooling at toe</li><li>PUMA Formstrip at lateral and medial sides</li><li>PUMA Suede callout in metallic foil at lateral side</li><li>Woven PUMA Logo Label at tongue</li></ul>", "magento_product_id": "85679", "attributes_hierarchy": "Women-Casual Shoes", "magento_parent_product_id": null}	["https://media.frankandoak.com/media/catalog/product/03-2017b/2820003-010.165977_1.jpg", "https://media.frankandoak.com/media/catalog/product/03-2017b/2820003-010.166072_2.jpg", "https://media.frankandoak.com/media/catalog/product/03-2017b/2820003-010.166078_3.jpg"]
        product.setName("Puma Platform Suede Core in White");
        product.setSku("63-2820003-010");
        product.setSummary("A women’s-only silhouette, it boasts a thick platform sole");
        product.setActive(true);

        Category category = new Category();
        category.setName("Shoes");
        category.setActive(true);
        category.setVersion(1);
        product.setCategories(Set.of(category));

        var productResult = productService.create(product);
        assertThat(productResult.getId()).isGreaterThan(0);
    }

}