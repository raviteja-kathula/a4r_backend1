package com.commerce4retail.product.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    private String id;
    private String parentId;

    @NotBlank(message = "Category name is required")
    @Size(max = 200)
    private String name;

    @NotBlank(message = "Slug is required")
    @Size(max = 200)
    private String slug;

    private String description;
    private String imageUrl;
    private String bannerUrl;
    private Integer displayOrder;
    private Boolean isActive;
    private String seoTitle;
    private String seoDescription;

    private String createdAt;
    private String updatedAt;
}
