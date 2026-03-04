package com.commerce4retail.product.repository;

import com.commerce4retail.product.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, String id);

    List<Category> findByParentIsNullAndIsActiveTrueOrderByDisplayOrderAsc();

    Page<Category> findByParentId(String parentId, Pageable pageable);

    Page<Category> findByIsActiveTrue(Pageable pageable);
}
