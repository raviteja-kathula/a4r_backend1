package com.commerce4retail.product.service;

import com.commerce4retail.product.dto.*;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {

    PagedResponse<CategoryDto> getAllCategories(boolean activeOnly, Pageable pageable);

    List<CategoryDto> getRootCategories();

    CategoryDto getCategoryById(String id);

    CategoryDto getCategoryBySlug(String slug);

    CategoryDto createCategory(CategoryDto request);

    CategoryDto updateCategory(String id, CategoryDto request);

    void deleteCategory(String id);
}
