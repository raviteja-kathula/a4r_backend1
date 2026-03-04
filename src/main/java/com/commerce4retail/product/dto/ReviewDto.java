package com.commerce4retail.product.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {

    private String id;
    private String productId;
    private String userId;
    private String userName;
    private Integer rating;
    private String title;
    private String comment;
    private Boolean verifiedPurchase;
    private Integer helpfulCount;
    private Integer notHelpfulCount;
    private String createdAt;
    private String updatedAt;
}
