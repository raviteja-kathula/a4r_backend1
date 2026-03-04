package com.commerce4retail.product.repository;

import com.commerce4retail.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, String> {

    List<ProductImage> findByProductIdOrderByDisplayOrderAsc(String productId);

    Optional<ProductImage> findByProductIdAndIsPrimaryTrue(String productId);
}
