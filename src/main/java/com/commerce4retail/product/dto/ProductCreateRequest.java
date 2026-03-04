package com.commerce4retail.product.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreateRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 300)
    private String name;

    @NotBlank(message = "Slug is required")
    @Size(max = 300)
    private String slug;

    @NotBlank(message = "SKU is required")
    @Size(max = 100)
    private String sku;

    private String brandId;
    private String categoryId;

    private String descriptionShort;
    private String descriptionLong;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", message = "Base price must be non-negative")
    private BigDecimal basePrice;

    private BigDecimal compareAtPrice;

    private String status;
}
