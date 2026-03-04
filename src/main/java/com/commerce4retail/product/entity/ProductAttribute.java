package com.commerce4retail.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "product_attributes", indexes = {
        @Index(name = "idx_pattr_product", columnList = "product_id"),
        @Index(name = "idx_pattr_name", columnList = "attribute_name"),
        @Index(name = "idx_pattr_composite", columnList = "attribute_name, attribute_value")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductAttribute {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotBlank
    @Column(name = "attribute_name", nullable = false, length = 100)
    private String attributeName;

    @NotBlank
    @Column(name = "attribute_value", nullable = false, length = 200)
    private String attributeValue;
}
