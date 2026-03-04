package com.commerce4retail.product.repository;

import com.commerce4retail.product.entity.Product;
import com.commerce4retail.product.entity.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    Optional<Product> findBySlugAndDeletedAtIsNull(String slug);

    Optional<Product> findByIdAndDeletedAtIsNull(String id);

    boolean existsBySlugAndDeletedAtIsNull(String slug);

    boolean existsBySlugAndIdNotAndDeletedAtIsNull(String slug, String id);

    boolean existsBySkuAndDeletedAtIsNull(String sku);

    boolean existsBySkuAndIdNotAndDeletedAtIsNull(String sku, String id);

    @Query("""
            SELECT p FROM Product p
            WHERE p.deletedAt IS NULL
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:brandId IS NULL OR p.brand.id = :brandId)
              AND (:status IS NULL OR p.status = :status)
              AND (:minPrice IS NULL OR p.basePrice >= :minPrice)
              AND (:maxPrice IS NULL OR p.basePrice <= :maxPrice)
              AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
                                   OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Product> findAllWithFilters(
            @Param("categoryId") String categoryId,
            @Param("brandId") String brandId,
            @Param("status") ProductStatus status,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("search") String search,
            Pageable pageable);
}
