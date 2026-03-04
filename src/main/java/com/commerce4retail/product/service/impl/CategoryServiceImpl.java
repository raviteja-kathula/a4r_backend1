package com.commerce4retail.product.service.impl;

import com.commerce4retail.product.dto.CategoryDto;
import com.commerce4retail.product.dto.PagedResponse;
import com.commerce4retail.product.entity.Category;
import com.commerce4retail.product.exception.BadRequestException;
import com.commerce4retail.product.exception.ResourceNotFoundException;
import com.commerce4retail.product.repository.CategoryRepository;
import com.commerce4retail.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public PagedResponse<CategoryDto> getAllCategories(boolean activeOnly, Pageable pageable) {
        Page<Category> page = activeOnly
                ? categoryRepository.findByIsActiveTrue(pageable)
                : categoryRepository.findAll(pageable);

        List<CategoryDto> items = page.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return PagedResponse.<CategoryDto>builder()
                .content(items)
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    @Override
    public List<CategoryDto> getRootCategories() {
        return categoryRepository.findByParentIsNullAndIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoryById(String id) {
        return toDto(findOrThrow(id));
    }

    @Override
    public CategoryDto getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with slug: " + slug));
        return toDto(category);
    }

    @Override
    @Transactional
    public CategoryDto createCategory(CategoryDto request) {
        if (categoryRepository.existsBySlug(request.getSlug())) {
            throw new BadRequestException("Category with slug '" + request.getSlug() + "' already exists");
        }
        Category category = Category.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .bannerUrl(request.getBannerUrl())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .seoTitle(request.getSeoTitle())
                .seoDescription(request.getSeoDescription())
                .build();

        if (request.getParentId() != null) {
            Category parent = findOrThrow(request.getParentId());
            category.setParent(parent);
        }

        Category saved = categoryRepository.save(category);
        log.info("Created category id={}", saved.getId());
        return toDto(saved);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(String id, CategoryDto request) {
        Category category = findOrThrow(id);

        if (request.getSlug() != null && !request.getSlug().equals(category.getSlug())) {
            if (categoryRepository.existsBySlugAndIdNot(request.getSlug(), id)) {
                throw new BadRequestException("Slug '" + request.getSlug() + "' is already in use");
            }
            category.setSlug(request.getSlug());
        }
        if (request.getName() != null)           category.setName(request.getName());
        if (request.getDescription() != null)    category.setDescription(request.getDescription());
        if (request.getImageUrl() != null)       category.setImageUrl(request.getImageUrl());
        if (request.getBannerUrl() != null)      category.setBannerUrl(request.getBannerUrl());
        if (request.getDisplayOrder() != null)   category.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null)       category.setIsActive(request.getIsActive());
        if (request.getSeoTitle() != null)       category.setSeoTitle(request.getSeoTitle());
        if (request.getSeoDescription() != null) category.setSeoDescription(request.getSeoDescription());
        if (request.getParentId() != null) {
            Category parent = findOrThrow(request.getParentId());
            category.setParent(parent);
        }

        Category saved = categoryRepository.save(category);
        log.info("Updated category id={}", saved.getId());
        return toDto(saved);
    }

    @Override
    @Transactional
    public void deleteCategory(String id) {
        Category category = findOrThrow(id);
        categoryRepository.delete(category);
        log.info("Deleted category id={}", id);
    }

    private Category findOrThrow(String id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    private CategoryDto toDto(Category c) {
        return CategoryDto.builder()
                .id(c.getId())
                .parentId(c.getParent() != null ? c.getParent().getId() : null)
                .name(c.getName())
                .slug(c.getSlug())
                .description(c.getDescription())
                .imageUrl(c.getImageUrl())
                .bannerUrl(c.getBannerUrl())
                .displayOrder(c.getDisplayOrder())
                .isActive(c.getIsActive())
                .seoTitle(c.getSeoTitle())
                .seoDescription(c.getSeoDescription())
                .createdAt(c.getCreatedAt() != null ? c.getCreatedAt().toString() : null)
                .updatedAt(c.getUpdatedAt() != null ? c.getUpdatedAt().toString() : null)
                .build();
    }
}
