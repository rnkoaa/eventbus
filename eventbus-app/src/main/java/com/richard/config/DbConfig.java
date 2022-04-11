package com.richard.config;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("db")
public class DbConfig {
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
