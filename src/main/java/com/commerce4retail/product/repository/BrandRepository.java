package com.commerce4retail.product.repository;

import com.commerce4retail.product.entity.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, String> {

    Optional<Brand> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, String id);

    Page<Brand> findByIsActiveTrue(Pageable pageable);
}
