package com.commerce4retail.product.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandDto {

    private String id;

    @NotBlank(message = "Brand name is required")
    @Size(max = 200)
    private String name;

    @NotBlank(message = "Slug is required")
    @Size(max = 200)
    private String slug;

    @Size(max = 500)
    private String logoUrl;

    private String description;

    private Boolean isActive;

    private String createdAt;
    private String updatedAt;
}
