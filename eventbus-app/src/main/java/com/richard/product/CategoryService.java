package com.richard.product;

import com.excalibur.product.tables.records.CategoryRecord;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.exception.DataAccessException;

import java.time.Instant;

import static com.excalibur.product.Tables.CATEGORY;
import static com.excalibur.product.Tables.PRODUCT;

@Singleton
public class CategoryService {

    private final DSLContext dslContext;

    public CategoryService(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public Category save(Category category) {
        CategoryRecord categoryRecord = new CategoryRecord()
                .setName(category.getName())
                .setCreatedAt(category.getCreatedAt().toString())
                .setUpdatedAt(category.getUpdatedAt().toString())
                .setActive(category.isActive())
                .setVersion(category.getVersion());


        try (var res = dslContext.insertInto(CATEGORY).set(categoryRecord)) {
            try {
                var categoryId = res
                        .returningResult(CATEGORY.ID)
                        .fetchOne(CATEGORY.ID);
                if (categoryId != null) {
                    category.setId(categoryId);
                }
            } catch (DataAccessException ex) {
                try (var res2 = dslContext.selectFrom(CATEGORY)
                        .where(CATEGORY.NAME.eq(category.getName()))) {
                    Integer categoryId = res2.fetchOne(CATEGORY.ID);
                    if (categoryId != null) {
                        category.setId(categoryId);
                    }
                }
            }
        }
        return category;
    }
}
