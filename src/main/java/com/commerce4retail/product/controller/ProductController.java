package com.commerce4retail.product.controller;

import com.commerce4retail.product.dto.*;
import com.commerce4retail.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management endpoints")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "List all products with optional filters, sorting, and pagination")
    public ResponseEntity<ApiResponse<PagedResponse<ProductListItemDto>>> getAllProducts(
            @Parameter(description = "Filter by category ID") @RequestParam(required = false) String categoryId,
            @Parameter(description = "Filter by brand ID")    @RequestParam(required = false) String brandId,
            @Parameter(description = "Filter by status (ACTIVE|DRAFT|DISCONTINUED)") @RequestParam(required = false) String status,
            @Parameter(description = "Minimum price filter")  @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price filter")  @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Search by name or SKU") @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0")    int page,
            @RequestParam(defaultValue = "20")   int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PagedResponse<ProductListItemDto> result = productService.getAllProducts(
                categoryId, brandId, status, minPrice, maxPrice, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ApiResponse<ProductDetailDto>> getProductById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductById(id)));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get product by slug")
    public ResponseEntity<ApiResponse<ProductDetailDto>> getProductBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductBySlug(slug)));
    }

    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<ApiResponse<ProductDetailDto>> createProduct(
            @Valid @RequestBody ProductCreateRequest request) {
        ProductDetailDto created = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Product created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing product")
    public ResponseEntity<ApiResponse<ProductDetailDto>> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", productService.updateProduct(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a product")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null));
    }

    @GetMapping("/{productId}/reviews")
    @Operation(summary = "Get reviews for a product")
    public ResponseEntity<ApiResponse<PagedResponse<ReviewDto>>> getProductReviews(
            @PathVariable String productId,
            @RequestParam(defaultValue = "0")    int page,
            @RequestParam(defaultValue = "10")   int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success(productService.getProductReviews(productId, pageable)));
    }

    @PostMapping("/{productId}/reviews")
    @Operation(summary = "Add a review to a product")
    public ResponseEntity<ApiResponse<ReviewDto>> addReview(
            @PathVariable String productId,
            @Valid @RequestBody ReviewCreateRequest request) {
        ReviewDto review = productService.addReview(productId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Review added successfully", review));
    }
}
