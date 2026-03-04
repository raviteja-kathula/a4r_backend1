package com.commerce4retail.product.service;

import com.commerce4retail.product.dto.*;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ProductService {

    PagedResponse<ProductListItemDto> getAllProducts(
            String categoryId, String brandId, String status,
            BigDecimal minPrice, BigDecimal maxPrice,
            String search, Pageable pageable);

    ProductDetailDto getProductById(String id);

    ProductDetailDto getProductBySlug(String slug);

    ProductDetailDto createProduct(ProductCreateRequest request);

    ProductDetailDto updateProduct(String id, ProductUpdateRequest request);

    void deleteProduct(String id);

    PagedResponse<ReviewDto> getProductReviews(String productId, Pageable pageable);

    ReviewDto addReview(String productId, ReviewCreateRequest request);
}
