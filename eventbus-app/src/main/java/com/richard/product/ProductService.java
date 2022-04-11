package com.richard.product;

import com.excalibur.product.tables.records.ProductRecord;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.excalibur.product.Tables.PRODUCT;

// https://www.petrikainulainen.net/programming/jooq/jooq-tips-implementing-a-read-only-one-to-many-relationship/
// https://blog.jooq.org/use-resultquery-collect-to-implement-powerful-mappings/
@Singleton
public class ProductService {

    private final DSLContext dslContext;

    public ProductService(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public List<Product> find() {
        return List.of();
    }

    public Product create(Product product) {
        var productRecord = new ProductRecord();
        productRecord.setTitle("Puma Platform Suede Core in White");
        productRecord.setSku("1-e0286");
        productRecord.setDescription("<p>Born in the late '60s and rocked by generations, the Suede is arguably PUMA’s most iconic sneaker. Today, we’re taking its sport-inspired swagger to new heights (literally) with the Suede Platform. A women’s-only silhouette, it boasts a thick platform sole, ridged tooling, and plenty of edge. It’s a gamechanger of a sneaker… for women who are ready to change the game.</p><ul><li>Suede upper with perf detailing at midfoot</li><li>Lace closure for a snug fit</li><li>Platform rubber outsole with ridged tooling at toe</li><li>PUMA Formstrip at lateral and medial sides</li><li>PUMA Suede callout in metallic foil at lateral side</li><li>Woven PUMA Logo Label at tongue</li></ul>");
        productRecord.setSummary("<p>Born in the late '60s and rocked by generations, the Suede is arguably PUMA’s most iconic sneaker. Today, we’re taking its sport-inspired swagger to new heights (literally) with the Suede Platform. A women’s-only silhouette, it boasts a thick platform sole, ridged tooling, and plenty of edge. It’s a gamechanger of a sneaker… for women who are ready to change the game.</p><ul><li>Suede upper with perf detailing at midfoot</li><li>Lace closure for a snug fit</li><li>Platform rubber outsole with ridged tooling at toe</li><li>PUMA Formstrip at lateral and medial sides</li><li>PUMA Suede callout in metallic foil at lateral side</li><li>Woven PUMA Logo Label at tongue</li></ul>");
        productRecord.setActive(true);

        productRecord.setCreatedAt(Instant.now().toString());
        productRecord.setUpdatedAt(Instant.now().toString());

//        int results = dslContext.insertInto(Tables.PRODUCT)
//                .set(productRecord)
//                .execute();
//        dslContext.insertInto(PRODUCT, PRODUCT.TITLE, PRODUCT.SKU, PRODUCT.DESCRIPTION,
//                        PRODUCT.SUMMARY, PRODUCT.IS_ACTIVE, PRODUCT.CREATED_ON, PRODUCT.UPDATED_AT,
//                        PRODUCT.VERSION)
//                .valuesOfRecords(
//                       productRecord.getTitle(), productRecord.getSku(), productRecord.getDescription(),
//                       productRecord.getSummary(), productRecord.getIsActive(), productRecord.getCreatedOn(),
//                       productRecord.getUpdatedAt()
//                )
        try (var res = dslContext.insertInto(PRODUCT).set(productRecord)) {
            var result = res.returning(PRODUCT.ID)
                    .fetchOne();
            if (result != null && result.getId() > 0) {
                product.setId(result.getId());
            }
        }
        return product;
    }

    public Optional<Product> findOne(int id) {
        return Optional.empty();
    }

    public Product update(Product product) {
        return null;
    }
}
