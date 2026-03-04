package com.commerce4retail.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "product_variant_attributes", indexes = {
        @Index(name = "idx_pvattr_variant", columnList = "variant_id"),
        @Index(name = "idx_pvattr_name", columnList = "attribute_name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantAttribute {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @NotBlank
    @Column(name = "attribute_name", nullable = false, length = 100)
    private String attributeName;

    @NotBlank
    @Column(name = "attribute_value", nullable = false, length = 200)
    private String attributeValue;
}
