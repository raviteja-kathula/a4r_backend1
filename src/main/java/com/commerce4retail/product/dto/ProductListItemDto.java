package com.commerce4retail.product.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListItemDto {

    private String id;
    private String name;
    private String slug;
    private String sku;
    private String status;

    // Brand
    private String brandId;
    private String brandName;

    // Category
    private String categoryId;
    private String categoryName;

    // Pricing
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private BigDecimal discountAmount;
    private BigDecimal discountPercentage;
    private String currency;

    // Media
    private String thumbnailUrl;

    // Inventory (derived from default variant)
    private boolean inStock;
    private Integer stockQuantity;

    // Ratings
    private Double averageRating;
    private Long reviewCount;

    // Misc
    private Integer variantCount;
    private String createdAt;
    private String updatedAt;
}
