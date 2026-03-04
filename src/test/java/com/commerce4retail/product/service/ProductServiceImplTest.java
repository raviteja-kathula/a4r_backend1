package com.commerce4retail.product.service;

import com.commerce4retail.product.dto.*;
import com.commerce4retail.product.entity.*;
import com.commerce4retail.product.entity.enums.ProductStatus;
import com.commerce4retail.product.exception.BadRequestException;
import com.commerce4retail.product.exception.ResourceNotFoundException;
import com.commerce4retail.product.repository.*;
import com.commerce4retail.product.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl Tests")
class ProductServiceImplTest {

    @Mock private ProductRepository productRepository;
    @Mock private BrandRepository brandRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private ProductImageRepository productImageRepository;
    @Mock private ProductVariantRepository productVariantRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product sampleProduct;
    private Brand sampleBrand;
    private Category sampleCategory;

    @BeforeEach
    void setUp() {
        sampleBrand = Brand.builder()
                .id("brand-1").name("Nike").slug("nike").isActive(true)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        sampleCategory = Category.builder()
                .id("cat-1").name("Shirts").slug("shirts").isActive(true)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        sampleProduct = Product.builder()
                .id("prod-1")
                .name("Test Tee")
                .slug("test-tee")
                .sku("TEST-001")
                .brand(sampleBrand)
                .category(sampleCategory)
                .basePrice(new BigDecimal("29.99"))
                .status(ProductStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ─── getAllProducts ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAllProducts")
    class GetAllProducts {

        @Test
        @DisplayName("returns paged response with correct metadata")
        void getAllProducts_returnsPagedResponse() {
            Page<Product> page = new PageImpl<>(List.of(sampleProduct), PageRequest.of(0, 20), 1);
            when(productRepository.findAllWithFilters(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any()))
                    .thenReturn(page);
            when(productImageRepository.findByProductIdAndIsPrimaryTrue(any())).thenReturn(Optional.empty());
            when(productVariantRepository.findByProductIdAndIsDefaultTrue(any())).thenReturn(Optional.empty());
            when(productVariantRepository.findByProductId(any())).thenReturn(Collections.emptyList());
            when(reviewRepository.findAverageRatingByProductId(any())).thenReturn(null);
            when(reviewRepository.countByProductId(any())).thenReturn(0L);

            PagedResponse<ProductListItemDto> result = productService.getAllProducts(
                    null, null, null, null, null, null, PageRequest.of(0, 20));

            assertThat(result.getTotalItems()).isEqualTo(1);
            assertThat(result.getCurrentPage()).isEqualTo(0);
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Test Tee");
        }

        @Test
        @DisplayName("throws BadRequestException for invalid status")
        void getAllProducts_invalidStatus_throwsBadRequest() {
            assertThatThrownBy(() ->
                    productService.getAllProducts(null, null, "INVALID", null, null, null, PageRequest.of(0, 20)))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Invalid status value");
        }
    }

    // ─── getProductById ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("getProductById")
    class GetProductById {

        @Test
        @DisplayName("returns product detail when found")
        void getProductById_found_returnsDetail() {
            when(productRepository.findByIdAndDeletedAtIsNull("prod-1")).thenReturn(Optional.of(sampleProduct));
            when(productImageRepository.findByProductIdOrderByDisplayOrderAsc("prod-1")).thenReturn(Collections.emptyList());
            when(productVariantRepository.findByProductId("prod-1")).thenReturn(Collections.emptyList());
            when(reviewRepository.findAverageRatingByProductId("prod-1")).thenReturn(4.5);
            when(reviewRepository.countByProductId("prod-1")).thenReturn(10L);

            ProductDetailDto dto = productService.getProductById("prod-1");

            assertThat(dto.getId()).isEqualTo("prod-1");
            assertThat(dto.getName()).isEqualTo("Test Tee");
            assertThat(dto.getAverageRating()).isEqualTo(4.5);
            assertThat(dto.getReviewCount()).isEqualTo(10L);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when not found")
        void getProductById_notFound_throws() {
            when(productRepository.findByIdAndDeletedAtIsNull("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getProductById("missing"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product not found");
        }
    }

    // ─── getProductBySlug ────────────────────────────────────────────────────

    @Nested
    @DisplayName("getProductBySlug")
    class GetProductBySlug {

        @Test
        @DisplayName("returns product detail when slug found")
        void getProductBySlug_found_returnsDetail() {
            when(productRepository.findBySlugAndDeletedAtIsNull("test-tee")).thenReturn(Optional.of(sampleProduct));
            when(productImageRepository.findByProductIdOrderByDisplayOrderAsc(any())).thenReturn(Collections.emptyList());
            when(productVariantRepository.findByProductId(any())).thenReturn(Collections.emptyList());
            when(reviewRepository.findAverageRatingByProductId(any())).thenReturn(null);
            when(reviewRepository.countByProductId(any())).thenReturn(0L);

            ProductDetailDto dto = productService.getProductBySlug("test-tee");

            assertThat(dto.getSlug()).isEqualTo("test-tee");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when slug not found")
        void getProductBySlug_notFound_throws() {
            when(productRepository.findBySlugAndDeletedAtIsNull("no-slug")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> productService.getProductBySlug("no-slug"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─── createProduct ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("createProduct")
    class CreateProduct {

        @Test
        @DisplayName("creates product successfully")
        void createProduct_success() {
            ProductCreateRequest req = ProductCreateRequest.builder()
                    .name("New Tee").slug("new-tee").sku("NEW-001")
                    .basePrice(new BigDecimal("25.00")).status("ACTIVE").build();

            when(productRepository.existsBySlugAndDeletedAtIsNull("new-tee")).thenReturn(false);
            when(productRepository.existsBySkuAndDeletedAtIsNull("NEW-001")).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
                Product p = inv.getArgument(0);
                p.setId("new-prod-1");
                p.setCreatedAt(LocalDateTime.now());
                p.setUpdatedAt(LocalDateTime.now());
                return p;
            });
            when(productImageRepository.findByProductIdOrderByDisplayOrderAsc(any())).thenReturn(Collections.emptyList());
            when(productVariantRepository.findByProductId(any())).thenReturn(Collections.emptyList());
            when(reviewRepository.findAverageRatingByProductId(any())).thenReturn(null);
            when(reviewRepository.countByProductId(any())).thenReturn(0L);

            ProductDetailDto result = productService.createProduct(req);

            assertThat(result.getName()).isEqualTo("New Tee");
            assertThat(result.getStatus()).isEqualTo("ACTIVE");
            verify(productRepository, times(1)).save(any(Product.class));
        }

        @Test
        @DisplayName("throws BadRequestException when slug already exists")
        void createProduct_duplicateSlug_throws() {
            ProductCreateRequest req = ProductCreateRequest.builder()
                    .name("Test").slug("test-tee").sku("UNIQUE-001")
                    .basePrice(BigDecimal.TEN).build();
            when(productRepository.existsBySlugAndDeletedAtIsNull("test-tee")).thenReturn(true);

            assertThatThrownBy(() -> productService.createProduct(req))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("throws BadRequestException when SKU already exists")
        void createProduct_duplicateSku_throws() {
            ProductCreateRequest req = ProductCreateRequest.builder()
                    .name("Test").slug("unique-slug").sku("TEST-001")
                    .basePrice(BigDecimal.TEN).build();
            when(productRepository.existsBySlugAndDeletedAtIsNull("unique-slug")).thenReturn(false);
            when(productRepository.existsBySkuAndDeletedAtIsNull("TEST-001")).thenReturn(true);

            assertThatThrownBy(() -> productService.createProduct(req))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException for non-existent brandId")
        void createProduct_invalidBrandId_throws() {
            ProductCreateRequest req = ProductCreateRequest.builder()
                    .name("Test").slug("slug-x").sku("SKU-X")
                    .basePrice(BigDecimal.TEN).brandId("bad-brand").build();
            when(productRepository.existsBySlugAndDeletedAtIsNull(any())).thenReturn(false);
            when(productRepository.existsBySkuAndDeletedAtIsNull(any())).thenReturn(false);
            when(brandRepository.findById("bad-brand")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.createProduct(req))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Brand not found");
        }
    }

    // ─── updateProduct ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateProduct")
    class UpdateProduct {

        @Test
        @DisplayName("updates product name successfully")
        void updateProduct_name_success() {
            ProductUpdateRequest req = ProductUpdateRequest.builder().name("Updated Tee").build();

            when(productRepository.findByIdAndDeletedAtIsNull("prod-1")).thenReturn(Optional.of(sampleProduct));
            when(productRepository.save(any())).thenReturn(sampleProduct);
            when(productImageRepository.findByProductIdOrderByDisplayOrderAsc(any())).thenReturn(Collections.emptyList());
            when(productVariantRepository.findByProductId(any())).thenReturn(Collections.emptyList());
            when(reviewRepository.findAverageRatingByProductId(any())).thenReturn(null);
            when(reviewRepository.countByProductId(any())).thenReturn(0L);

            ProductDetailDto result = productService.updateProduct("prod-1", req);

            assertThat(result).isNotNull();
            verify(productRepository).save(any());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when product not found")
        void updateProduct_notFound_throws() {
            when(productRepository.findByIdAndDeletedAtIsNull("ghost")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> productService.updateProduct("ghost", new ProductUpdateRequest()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─── deleteProduct ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteProduct")
    class DeleteProduct {

        @Test
        @DisplayName("soft-deletes product by setting deletedAt")
        void deleteProduct_softDeletes() {
            when(productRepository.findByIdAndDeletedAtIsNull("prod-1")).thenReturn(Optional.of(sampleProduct));
            when(productRepository.save(any())).thenReturn(sampleProduct);

            productService.deleteProduct("prod-1");

            assertThat(sampleProduct.getDeletedAt()).isNotNull();
            verify(productRepository).save(sampleProduct);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when product not found")
        void deleteProduct_notFound_throws() {
            when(productRepository.findByIdAndDeletedAtIsNull("nope")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> productService.deleteProduct("nope"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─── getProductReviews ───────────────────────────────────────────────────

    @Nested
    @DisplayName("getProductReviews")
    class GetProductReviews {

        @Test
        @DisplayName("returns paged reviews for existing product")
        void getProductReviews_success() {
            when(productRepository.findByIdAndDeletedAtIsNull("prod-1")).thenReturn(Optional.of(sampleProduct));
            Review review = Review.builder()
                    .id("rev-1").product(sampleProduct).userId("usr-1").userName("Alice")
                    .rating(5).title("Great").comment("Loved it.").verifiedPurchase(true)
                    .helpfulCount(3).notHelpfulCount(0).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                    .build();
            Page<Review> page = new PageImpl<>(List.of(review));
            when(reviewRepository.findByProductId(eq("prod-1"), any())).thenReturn(page);

            PagedResponse<ReviewDto> result = productService.getProductReviews("prod-1", PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getUserName()).isEqualTo("Alice");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when product not found")
        void getProductReviews_productNotFound_throws() {
            when(productRepository.findByIdAndDeletedAtIsNull("bad")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> productService.getProductReviews("bad", PageRequest.of(0, 10)))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─── addReview ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("addReview")
    class AddReview {

        @Test
        @DisplayName("adds review to product successfully")
        void addReview_success() {
            ReviewCreateRequest req = ReviewCreateRequest.builder()
                    .userId("usr-2").userName("Bob").rating(4)
                    .title("Good").comment("Nice product.").build();

            when(productRepository.findByIdAndDeletedAtIsNull("prod-1")).thenReturn(Optional.of(sampleProduct));
            Review saved = Review.builder()
                    .id("rev-new").product(sampleProduct).userId("usr-2").userName("Bob")
                    .rating(4).title("Good").comment("Nice product.")
                    .verifiedPurchase(false).helpfulCount(0).notHelpfulCount(0)
                    .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                    .build();
            when(reviewRepository.save(any())).thenReturn(saved);

            ReviewDto result = productService.addReview("prod-1", req);

            assertThat(result.getRating()).isEqualTo(4);
            assertThat(result.getUserName()).isEqualTo("Bob");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when product not found")
        void addReview_productNotFound_throws() {
            when(productRepository.findByIdAndDeletedAtIsNull("bad")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> productService.addReview("bad", new ReviewCreateRequest()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
