package com.richard.product;

import com.excalibur.product.tables.records.CategoryRecord;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;

import static com.excalibur.product.Tables.CATEGORY;

@Singleton
public class CategoryService {
    static String UNIQUE_CONSTRAINT_EXCEPTION = "UNIQUE constraint failed";

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
                if (ex.getMessage().contains(UNIQUE_CONSTRAINT_EXCEPTION)) {
                    try (var res2 = dslContext.selectFrom(CATEGORY)
                            .where(CATEGORY.NAME.eq(category.getName()))) {
                        Integer categoryId = res2.fetchOne(CATEGORY.ID);
                        if (categoryId != null) {
                            category.setId(categoryId);
                        }
                    }
                }
            }
        }
        return category;
    }
}
