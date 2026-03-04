package com.commerce4retail.product.service.impl;

import com.commerce4retail.product.dto.*;
import com.commerce4retail.product.entity.*;
import com.commerce4retail.product.entity.enums.ProductStatus;
import com.commerce4retail.product.exception.BadRequestException;
import com.commerce4retail.product.exception.ResourceNotFoundException;
import com.commerce4retail.product.repository.*;
import com.commerce4retail.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    public PagedResponse<ProductListItemDto> getAllProducts(
            String categoryId, String brandId, String statusStr,
            BigDecimal minPrice, BigDecimal maxPrice,
            String search, Pageable pageable) {

        ProductStatus status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                status = ProductStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid status value: " + statusStr);
            }
        }

        Page<Product> page = productRepository.findAllWithFilters(
                categoryId, brandId, status, minPrice, maxPrice, search, pageable);

        List<ProductListItemDto> items = page.getContent().stream()
                .map(this::toListItemDto)
                .collect(Collectors.toList());

        return buildPagedResponse(items, page);
    }

    @Override
    public ProductDetailDto getProductById(String id) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return toDetailDto(product);
    }

    @Override
    public ProductDetailDto getProductBySlug(String slug) {
        Product product = productRepository.findBySlugAndDeletedAtIsNull(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));
        return toDetailDto(product);
    }

    @Override
    @Transactional
    public ProductDetailDto createProduct(ProductCreateRequest request) {
        if (productRepository.existsBySlugAndDeletedAtIsNull(request.getSlug())) {
            throw new BadRequestException("Product with slug '" + request.getSlug() + "' already exists");
        }
        if (productRepository.existsBySkuAndDeletedAtIsNull(request.getSku())) {
            throw new BadRequestException("Product with SKU '" + request.getSku() + "' already exists");
        }

        Product product = Product.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .sku(request.getSku())
                .descriptionShort(request.getDescriptionShort())
                .descriptionLong(request.getDescriptionLong())
                .basePrice(request.getBasePrice())
                .compareAtPrice(request.getCompareAtPrice())
                .status(request.getStatus() != null
                        ? ProductStatus.valueOf(request.getStatus().toUpperCase())
                        : ProductStatus.DRAFT)
                .build();

        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found: " + request.getBrandId()));
            product.setBrand(brand);
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.getCategoryId()));
            product.setCategory(category);
        }

        Product saved = productRepository.save(product);
        log.info("Created product id={}, sku={}", saved.getId(), saved.getSku());
        return toDetailDto(saved);
    }

    @Override
    @Transactional
    public ProductDetailDto updateProduct(String id, ProductUpdateRequest request) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (request.getSlug() != null && !request.getSlug().equals(product.getSlug())) {
            if (productRepository.existsBySlugAndIdNotAndDeletedAtIsNull(request.getSlug(), id)) {
                throw new BadRequestException("Slug '" + request.getSlug() + "' is already in use");
            }
            product.setSlug(request.getSlug());
        }
        if (request.getSku() != null && !request.getSku().equals(product.getSku())) {
            if (productRepository.existsBySkuAndIdNotAndDeletedAtIsNull(request.getSku(), id)) {
                throw new BadRequestException("SKU '" + request.getSku() + "' is already in use");
            }
            product.setSku(request.getSku());
        }
        if (request.getName() != null)              product.setName(request.getName());
        if (request.getDescriptionShort() != null)  product.setDescriptionShort(request.getDescriptionShort());
        if (request.getDescriptionLong() != null)   product.setDescriptionLong(request.getDescriptionLong());
        if (request.getBasePrice() != null)         product.setBasePrice(request.getBasePrice());
        if (request.getCompareAtPrice() != null)    product.setCompareAtPrice(request.getCompareAtPrice());
        if (request.getStatus() != null) {
            product.setStatus(ProductStatus.valueOf(request.getStatus().toUpperCase()));
        }
        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found: " + request.getBrandId()));
            product.setBrand(brand);
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.getCategoryId()));
            product.setCategory(category);
        }

        Product saved = productRepository.save(product);
        log.info("Updated product id={}", saved.getId());
        return toDetailDto(saved);
    }

    @Override
    @Transactional
    public void deleteProduct(String id) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        product.setDeletedAt(LocalDateTime.now());
        productRepository.save(product);
        log.info("Soft-deleted product id={}", id);
    }

    @Override
    public PagedResponse<ReviewDto> getProductReviews(String productId, Pageable pageable) {
        if (!productRepository.findByIdAndDeletedAtIsNull(productId).isPresent()) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }
        Page<Review> page = reviewRepository.findByProductId(productId, pageable);
        List<ReviewDto> items = page.getContent().stream()
                .map(this::toReviewDto)
                .collect(Collectors.toList());
        return buildPagedResponse(items, page);
    }

    @Override
    @Transactional
    public ReviewDto addReview(String productId, ReviewCreateRequest request) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        Review review = Review.builder()
                .product(product)
                .userId(request.getUserId())
                .userName(request.getUserName())
                .rating(request.getRating())
                .title(request.getTitle())
                .comment(request.getComment())
                .verifiedPurchase(request.getVerifiedPurchase() != null && request.getVerifiedPurchase())
                .build();

        Review saved = reviewRepository.save(review);
        log.info("Added review id={} to product id={}", saved.getId(), productId);
        return toReviewDto(saved);
    }

    // ─── Mapping Helpers ──────────────────────────────────────────────────────

    private ProductListItemDto toListItemDto(Product p) {
        String thumbnailUrl = productImageRepository
                .findByProductIdAndIsPrimaryTrue(p.getId())
                .map(ProductImage::getThumbnailUrl)
                .orElse(null);

        ProductVariant defaultVariant = productVariantRepository
                .findByProductIdAndIsDefaultTrue(p.getId())
                .orElse(null);

        boolean inStock = defaultVariant != null && defaultVariant.isInStock();
        Integer qty = defaultVariant != null ? defaultVariant.getQuantity() : null;

        Double avgRating = reviewRepository.findAverageRatingByProductId(p.getId());
        long reviewCount = reviewRepository.countByProductId(p.getId());
        long variantCount = productVariantRepository.findByProductId(p.getId()).size();

        BigDecimal discountAmt = null;
        BigDecimal discountPct = null;
        if (p.getCompareAtPrice() != null && p.getCompareAtPrice().compareTo(p.getBasePrice()) > 0) {
            discountAmt = p.getCompareAtPrice().subtract(p.getBasePrice());
            discountPct = discountAmt
                    .divide(p.getCompareAtPrice(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return ProductListItemDto.builder()
                .id(p.getId())
                .name(p.getName())
                .slug(p.getSlug())
                .sku(p.getSku())
                .status(p.getStatus().name())
                .brandId(p.getBrand() != null ? p.getBrand().getId() : null)
                .brandName(p.getBrand() != null ? p.getBrand().getName() : null)
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .price(p.getBasePrice())
                .compareAtPrice(p.getCompareAtPrice())
                .discountAmount(discountAmt)
                .discountPercentage(discountPct)
                .currency("USD")
                .thumbnailUrl(thumbnailUrl)
                .inStock(inStock)
                .stockQuantity(qty)
                .averageRating(avgRating)
                .reviewCount(reviewCount)
                .variantCount((int) variantCount)
                .createdAt(p.getCreatedAt() != null ? p.getCreatedAt().toString() : null)
                .updatedAt(p.getUpdatedAt() != null ? p.getUpdatedAt().toString() : null)
                .build();
    }

    private ProductDetailDto toDetailDto(Product p) {
        List<ProductImage> images = productImageRepository.findByProductIdOrderByDisplayOrderAsc(p.getId());
        List<ProductVariant> variants = productVariantRepository.findByProductId(p.getId());

        Double avgRating = reviewRepository.findAverageRatingByProductId(p.getId());
        long reviewCount = reviewRepository.countByProductId(p.getId());

        List<ProductDetailDto.ProductImageDto> imageDtos = images.stream()
                .map(img -> ProductDetailDto.ProductImageDto.builder()
                        .id(img.getId())
                        .url(img.getUrl())
                        .thumbnailUrl(img.getThumbnailUrl())
                        .altText(img.getAltText())
                        .isPrimary(img.getIsPrimary())
                        .displayOrder(img.getDisplayOrder())
                        .build())
                .collect(Collectors.toList());

        List<ProductDetailDto.ProductVariantDto> variantDtos = variants.stream()
                .map(v -> ProductDetailDto.ProductVariantDto.builder()
                        .id(v.getId())
                        .sku(v.getSku())
                        .price(v.getPrice())
                        .compareAtPrice(v.getCompareAtPrice())
                        .quantity(v.getQuantity())
                        .isDefault(v.getIsDefault())
                        .inStock(v.isInStock())
                        .attributes(v.getVariantAttributes().stream()
                                .map(a -> new ProductDetailDto.ProductAttributeDto(a.getAttributeName(), a.getAttributeValue()))
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        List<ProductDetailDto.ProductAttributeDto> attrDtos = p.getAttributes().stream()
                .map(a -> new ProductDetailDto.ProductAttributeDto(a.getAttributeName(), a.getAttributeValue()))
                .collect(Collectors.toList());

        BrandDto brandDto = p.getBrand() != null ? BrandDto.builder()
                .id(p.getBrand().getId())
                .name(p.getBrand().getName())
                .slug(p.getBrand().getSlug())
                .logoUrl(p.getBrand().getLogoUrl())
                .build() : null;

        CategoryDto categoryDto = p.getCategory() != null ? CategoryDto.builder()
                .id(p.getCategory().getId())
                .name(p.getCategory().getName())
                .slug(p.getCategory().getSlug())
                .build() : null;

        return ProductDetailDto.builder()
                .id(p.getId())
                .name(p.getName())
                .slug(p.getSlug())
                .sku(p.getSku())
                .status(p.getStatus().name())
                .brand(brandDto)
                .category(categoryDto)
                .descriptionShort(p.getDescriptionShort())
                .descriptionLong(p.getDescriptionLong())
                .basePrice(p.getBasePrice())
                .compareAtPrice(p.getCompareAtPrice())
                .currency("USD")
                .images(imageDtos)
                .variants(variantDtos)
                .attributes(attrDtos)
                .averageRating(avgRating)
                .reviewCount(reviewCount)
                .createdAt(p.getCreatedAt() != null ? p.getCreatedAt().toString() : null)
                .updatedAt(p.getUpdatedAt() != null ? p.getUpdatedAt().toString() : null)
                .build();
    }

    private ReviewDto toReviewDto(Review r) {
        return ReviewDto.builder()
                .id(r.getId())
                .productId(r.getProduct().getId())
                .userId(r.getUserId())
                .userName(r.getUserName())
                .rating(r.getRating())
                .title(r.getTitle())
                .comment(r.getComment())
                .verifiedPurchase(r.getVerifiedPurchase())
                .helpfulCount(r.getHelpfulCount())
                .notHelpfulCount(r.getNotHelpfulCount())
                .createdAt(r.getCreatedAt() != null ? r.getCreatedAt().toString() : null)
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : null)
                .build();
    }

    private <T> PagedResponse<T> buildPagedResponse(List<T> content, Page<?> page) {
        return PagedResponse.<T>builder()
                .content(content)
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}
