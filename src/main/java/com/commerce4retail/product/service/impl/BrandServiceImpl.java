package com.commerce4retail.product.service.impl;

import com.commerce4retail.product.dto.BrandDto;
import com.commerce4retail.product.dto.PagedResponse;
import com.commerce4retail.product.entity.Brand;
import com.commerce4retail.product.exception.BadRequestException;
import com.commerce4retail.product.exception.ResourceNotFoundException;
import com.commerce4retail.product.repository.BrandRepository;
import com.commerce4retail.product.service.BrandService;
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
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;

    @Override
    public PagedResponse<BrandDto> getAllBrands(boolean activeOnly, Pageable pageable) {
        Page<Brand> page = activeOnly
                ? brandRepository.findByIsActiveTrue(pageable)
                : brandRepository.findAll(pageable);

        List<BrandDto> items = page.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return PagedResponse.<BrandDto>builder()
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
    public BrandDto getBrandById(String id) {
        return toDto(findOrThrow(id));
    }

    @Override
    @Transactional
    public BrandDto createBrand(BrandDto request) {
        if (brandRepository.existsBySlug(request.getSlug())) {
            throw new BadRequestException("Brand with slug '" + request.getSlug() + "' already exists");
        }
        Brand brand = Brand.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .logoUrl(request.getLogoUrl())
                .description(request.getDescription())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        Brand saved = brandRepository.save(brand);
        log.info("Created brand id={}", saved.getId());
        return toDto(saved);
    }

    @Override
    @Transactional
    public BrandDto updateBrand(String id, BrandDto request) {
        Brand brand = findOrThrow(id);
        if (request.getSlug() != null && !request.getSlug().equals(brand.getSlug())) {
            if (brandRepository.existsBySlugAndIdNot(request.getSlug(), id)) {
                throw new BadRequestException("Slug '" + request.getSlug() + "' is already in use");
            }
            brand.setSlug(request.getSlug());
        }
        if (request.getName() != null)        brand.setName(request.getName());
        if (request.getLogoUrl() != null)     brand.setLogoUrl(request.getLogoUrl());
        if (request.getDescription() != null) brand.setDescription(request.getDescription());
        if (request.getIsActive() != null)    brand.setIsActive(request.getIsActive());

        Brand saved = brandRepository.save(brand);
        log.info("Updated brand id={}", saved.getId());
        return toDto(saved);
    }

    @Override
    @Transactional
    public void deleteBrand(String id) {
        Brand brand = findOrThrow(id);
        brandRepository.delete(brand);
        log.info("Deleted brand id={}", id);
    }

    private Brand findOrThrow(String id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));
    }

    private BrandDto toDto(Brand b) {
        return BrandDto.builder()
                .id(b.getId())
                .name(b.getName())
                .slug(b.getSlug())
                .logoUrl(b.getLogoUrl())
                .description(b.getDescription())
                .isActive(b.getIsActive())
                .createdAt(b.getCreatedAt() != null ? b.getCreatedAt().toString() : null)
                .updatedAt(b.getUpdatedAt() != null ? b.getUpdatedAt().toString() : null)
                .build();
    }
}
