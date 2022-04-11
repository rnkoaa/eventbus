package com.richard.product;

import java.math.BigDecimal;

public record Price(String currency, BigDecimal value) {
    public Price(BigDecimal value) {
        this("USD", value);
    }
}
