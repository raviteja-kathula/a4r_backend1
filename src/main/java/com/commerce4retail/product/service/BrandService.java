package com.commerce4retail.product.service;

import com.commerce4retail.product.dto.*;
import org.springframework.data.domain.Pageable;

public interface BrandService {

    PagedResponse<BrandDto> getAllBrands(boolean activeOnly, Pageable pageable);

    BrandDto getBrandById(String id);

    BrandDto createBrand(BrandDto request);

    BrandDto updateBrand(String id, BrandDto request);

    void deleteBrand(String id);
}
