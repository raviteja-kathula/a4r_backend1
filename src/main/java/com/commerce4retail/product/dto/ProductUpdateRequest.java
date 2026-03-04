package com.commerce4retail.product.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateRequest {

    @Size(max = 300)
    private String name;

    @Size(max = 300)
    private String slug;

    @Size(max = 100)
    private String sku;

    private String brandId;
    private String categoryId;

    private String descriptionShort;
    private String descriptionLong;

    @DecimalMin(value = "0.0", message = "Base price must be non-negative")
    private BigDecimal basePrice;

    private BigDecimal compareAtPrice;

    private String status;
}
