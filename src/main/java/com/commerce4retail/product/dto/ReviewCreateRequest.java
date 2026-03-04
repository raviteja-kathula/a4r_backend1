package com.commerce4retail.product.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewCreateRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "User name is required")
    @Size(max = 200)
    private String userName;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must not exceed 5")
    private Integer rating;

    @Size(max = 300)
    private String title;

    private String comment;

    private Boolean verifiedPurchase;
}
