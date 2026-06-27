package com.deportal.products.dto;

import com.deportal.products.enums.ProductType;
import java.math.BigDecimal;

public record ProductResponse(
        String productId,
        String name,
        String description,
        ProductType type,
        BigDecimal price,
        boolean active) {
}
