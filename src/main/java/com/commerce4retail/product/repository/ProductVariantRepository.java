package com.commerce4retail.product.repository;

import com.commerce4retail.product.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, String> {

    List<ProductVariant> findByProductId(String productId);

    Optional<ProductVariant> findByProductIdAndIsDefaultTrue(String productId);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, String id);
}
