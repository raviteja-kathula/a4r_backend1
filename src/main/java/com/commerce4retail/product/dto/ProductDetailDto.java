package com.commerce4retail.product.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailDto {

    private String id;
    private String name;
    private String slug;
    private String sku;
    private String status;

    // Brand
    private BrandDto brand;

    // Category
    private CategoryDto category;

    // Descriptions
    private String descriptionShort;
    private String descriptionLong;

    // Pricing
    private BigDecimal basePrice;
    private BigDecimal compareAtPrice;
    private String currency;

    // Media
    private List<ProductImageDto> images;

    // Variants
    private List<ProductVariantDto> variants;

    // Attributes
    private List<ProductAttributeDto> attributes;

    // Ratings summary
    private Double averageRating;
    private Long reviewCount;

    private String createdAt;
    private String updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductImageDto {
        private String id;
        private String url;
        private String thumbnailUrl;
        private String altText;
        private Boolean isPrimary;
        private Integer displayOrder;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductVariantDto {
        private String id;
        private String sku;
        private BigDecimal price;
        private BigDecimal compareAtPrice;
        private Integer quantity;
        private Boolean isDefault;
        private Boolean inStock;
        private List<ProductAttributeDto> attributes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductAttributeDto {
        private String attributeName;
        private String attributeValue;
    }
}
