package com.richard.product;

import com.excalibur.product.tables.records.CategoryRecord;
import com.excalibur.product.tables.records.ProductCategoryRecord;
import com.excalibur.product.tables.records.ProductRecord;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;
import org.jooq.JSON;
import org.jooq.exception.DataAccessException;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.excalibur.product.Tables.*;
import static com.richard.product.CategoryService.UNIQUE_CONSTRAINT_EXCEPTION;

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
        return dslContext.transactionResult(configuration -> {
            Set<Category> categories = product.getCategories();
            if (categories.size() > 0) {
                var categoryRecords = categories
                        .stream()
                        .map(it -> new CategoryRecord()
                                .setCreatedAt(it.getCreatedAt() != null ? it.getCreatedAt().toString() : Instant.now().toString())
                                .setUpdatedAt(it.getUpdatedAt() != null ? it.getUpdatedAt().toString() : Instant.now().toString())
                                .setName(it.getName())
                                .setActive(it.isActive())
                                .setVersion(it.getVersion())
                        )
                        .toList();
                Map<String, Integer> categoryRes = saveLocalCategories(categoryRecords);
                categories = categories.stream()
                        .peek(category ->
                                category.setId(Objects.requireNonNullElse(categoryRes.get(category.getName()), 0))
                        )
                        .collect(Collectors.toSet());
            }
            int productId = saveProductRecord(
                    new ProductRecord()
                            .setActive(product.isActive())
                            .setDescription(product.getDescription())
                            .setTitle(product.getName())
                            .setSummary(product.getSummary())
                            .setSku(product.getSku())
                            .setCreatedAt(product.getCreatedAt() != null ? product.getCreatedAt().toString() : Instant.now().toString())
                            .setUpdatedAt(product.getUpdatedAt() != null ? product.getUpdatedAt().toString() : Instant.now().toString())

                    // price
//                            .setPrice(JSON.valueOf())
            );
            if (productId > 0) {
                List<Integer> saved = categories
                        .stream()
                        .filter(category -> category.getId() > 0)
                        .map(category -> saveProductCategoryBridge(productId, category.getId()))
                        .toList();
                if (saved.size() != categories.size()) {
                    throw new RuntimeException("unable to save all product shoe category items for product " + productId);
                }
            }

            product.setId(productId);
            product.setCategories(categories);
            return product;
        });
    }

    private int saveProductCategoryBridge(int productId, Integer categoryId) {
        var productCategory = new ProductCategoryRecord()
                .setProductId(productId)
                .setCategoryId(categoryId);

        return dslContext.insertInto(PRODUCT_CATEGORY)
                .set(productCategory)
                .execute();

    }

    private Map<String, Integer> saveLocalCategories(List<CategoryRecord> categoryRecords) {
        return categoryRecords.stream()
                .collect(Collectors.toMap(CategoryRecord::getName, this::saveCategoryRecord));
    }

    int saveProductRecord(ProductRecord productRecord) {
        var result = dslContext
                .insertInto(PRODUCT)
                .set(productRecord)
                .returningResult(PRODUCT.ID)
                .fetchOne(PRODUCT.ID);
        if (result != null) {
            return result;
        }
        return 0;
    }

    int saveCategoryRecord(CategoryRecord categoryRecord) {
        try {
            var result = dslContext
                    .insertInto(CATEGORY)
                    .set(categoryRecord)
                    .returningResult(CATEGORY.ID)
                    .fetchOne(CATEGORY.ID);
            if (result != null) {
                return result;
            }
        } catch (DataAccessException ex) {
            if (ex.getMessage().contains(UNIQUE_CONSTRAINT_EXCEPTION)) {
                var result = dslContext.selectFrom(CATEGORY)
                        .where(CATEGORY.NAME.eq(categoryRecord.getName()))
                        .fetchOne(CATEGORY.ID);
                if (result != null) {
                    return result;
                }
            }
        }
        return 0;
    }

    public Optional<Product> findOne(int id) {
        return Optional.empty();
    }

    public Product update(Product product) {
        return null;
    }

    public boolean delete(int productId) {
        return dslContext.transactionResult(configuration -> {
            dslContext.delete(PRODUCT_CATEGORY)
                    .where(PRODUCT_CATEGORY.PRODUCT_ID.eq(productId))
                    .execute();

            var deleteResult = dslContext.delete(PRODUCT)
                    .where(PRODUCT.ID.eq(productId))
                    .execute();
            return deleteResult > 0;
        });
    }
}
