package com.richard.config;

import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Factory
public class DslContextFactory {

    @Singleton
    public DSLContext dslContext(DbConfig dbConfig) throws SQLException {
        final Connection connection = DriverManager.getConnection(dbConfig.getUrl());
        return DSL.using(connection, SQLDialect.SQLITE);
    }
}
