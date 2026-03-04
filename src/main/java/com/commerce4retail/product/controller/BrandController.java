package com.commerce4retail.product.controller;

import com.commerce4retail.product.dto.*;
import com.commerce4retail.product.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
@Tag(name = "Brands", description = "Brand management endpoints")
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    @Operation(summary = "List all brands")
    public ResponseEntity<ApiResponse<PagedResponse<BrandDto>>> getAllBrands(
            @RequestParam(defaultValue = "false") boolean activeOnly,
            @RequestParam(defaultValue = "0")     int page,
            @RequestParam(defaultValue = "20")    int size,
            @RequestParam(defaultValue = "name")  String sortBy,
            @RequestParam(defaultValue = "asc")   String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success(brandService.getAllBrands(activeOnly, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get brand by ID")
    public ResponseEntity<ApiResponse<BrandDto>> getBrandById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(brandService.getBrandById(id)));
    }

    @PostMapping
    @Operation(summary = "Create a new brand")
    public ResponseEntity<ApiResponse<BrandDto>> createBrand(@Valid @RequestBody BrandDto request) {
        BrandDto created = brandService.createBrand(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Brand created successfully", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing brand")
    public ResponseEntity<ApiResponse<BrandDto>> updateBrand(
            @PathVariable String id,
            @Valid @RequestBody BrandDto request) {
        return ResponseEntity.ok(ApiResponse.success("Brand updated successfully", brandService.updateBrand(id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a brand")
    public ResponseEntity<ApiResponse<Void>> deleteBrand(@PathVariable String id) {
        brandService.deleteBrand(id);
        return ResponseEntity.ok(ApiResponse.success("Brand deleted successfully", null));
    }
}
