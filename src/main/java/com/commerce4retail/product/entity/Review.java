package com.commerce4retail.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "idx_review_product", columnList = "product_id"),
        @Index(name = "idx_review_user", columnList = "user_id"),
        @Index(name = "idx_review_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotBlank
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @NotBlank
    @Column(name = "user_name", nullable = false, length = 200)
    private String userName;

    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer rating;

    @Column(length = 300)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "verified_purchase", nullable = false)
    @Builder.Default
    private Boolean verifiedPurchase = false;

    @Column(name = "helpful_count", nullable = false)
    @Builder.Default
    private Integer helpfulCount = 0;

    @Column(name = "not_helpful_count", nullable = false)
    @Builder.Default
    private Integer notHelpfulCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
