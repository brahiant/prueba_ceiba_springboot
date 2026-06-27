package com.deportal.products.mapper;

import com.deportal.products.dto.CreateProductRequest;
import com.deportal.products.dto.ProductResponse;
import com.deportal.products.entity.ProductEntity;
import com.deportal.shared.sanitization.StringSanitizer;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    private final StringSanitizer sanitizer;

    public ProductMapper(StringSanitizer sanitizer) {
        this.sanitizer = sanitizer;
    }

    public ProductEntity toEntity(CreateProductRequest request) {
        return new ProductEntity(
                sanitizer.clean(request.name()),
                sanitizer.clean(request.description()),
                request.type(),
                request.price(),
                true);
    }

    public ProductResponse toResponse(ProductEntity entity) {
        return new ProductResponse(
                entity.getProductId(),
                entity.getName(),
                entity.getDescription(),
                entity.getType(),
                entity.getPrice(),
                entity.isActive());
    }
}
