package com.commerce4retail.product.controller;

import com.commerce4retail.product.dto.*;
import com.commerce4retail.product.exception.ResourceNotFoundException;
import com.commerce4retail.product.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@DisplayName("ProductController Tests")
class ProductControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean ProductService productService;

    private ProductListItemDto sampleListItem;
    private ProductDetailDto sampleDetail;

    @BeforeEach
    void setUp() {
        sampleListItem = ProductListItemDto.builder()
                .id("p-1").name("Test Tee").slug("test-tee").sku("TST-001")
                .price(new BigDecimal("29.99")).inStock(true).status("ACTIVE")
                .build();

        sampleDetail = ProductDetailDto.builder()
                .id("p-1").name("Test Tee").slug("test-tee").sku("TST-001")
                .basePrice(new BigDecimal("29.99")).status("ACTIVE")
                .images(List.of()).variants(List.of()).attributes(List.of())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/products returns 200 with paged list")
    void getAllProducts_returns200() throws Exception {
        PagedResponse<ProductListItemDto> paged = PagedResponse.<ProductListItemDto>builder()
                .content(List.of(sampleListItem)).currentPage(0).pageSize(20)
                .totalItems(1).totalPages(1).hasNext(false).hasPrevious(false)
                .build();
        when(productService.getAllProducts(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(paged);

        mockMvc.perform(get("/api/v1/products").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalItems").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("Test Tee"));
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} returns 200 when found")
    void getProductById_found_returns200() throws Exception {
        when(productService.getProductById("p-1")).thenReturn(sampleDetail);

        mockMvc.perform(get("/api/v1/products/p-1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("p-1"))
                .andExpect(jsonPath("$.data.name").value("Test Tee"));
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} returns 404 when not found")
    void getProductById_notFound_returns404() throws Exception {
        when(productService.getProductById("missing"))
                .thenThrow(new ResourceNotFoundException("Product not found with id: missing"));

        mockMvc.perform(get("/api/v1/products/missing").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/products/slug/{slug} returns 200 when found")
    void getProductBySlug_returns200() throws Exception {
        when(productService.getProductBySlug("test-tee")).thenReturn(sampleDetail);

        mockMvc.perform(get("/api/v1/products/slug/test-tee").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.slug").value("test-tee"));
    }

    @Test
    @DisplayName("POST /api/v1/products returns 201 with created product")
    void createProduct_returns201() throws Exception {
        ProductCreateRequest req = ProductCreateRequest.builder()
                .name("New Tee").slug("new-tee").sku("NEW-001")
                .basePrice(new BigDecimal("25.00")).status("DRAFT").build();
        when(productService.createProduct(any())).thenReturn(sampleDetail);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/products returns 400 for invalid request (missing name)")
    void createProduct_invalidRequest_returns400() throws Exception {
        ProductCreateRequest req = ProductCreateRequest.builder()
                .slug("slug").sku("SKU").basePrice(BigDecimal.TEN).build(); // name missing

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/products/{id} returns 200 with updated product")
    void updateProduct_returns200() throws Exception {
        ProductUpdateRequest req = ProductUpdateRequest.builder().name("Updated").build();
        when(productService.updateProduct(eq("p-1"), any())).thenReturn(sampleDetail);

        mockMvc.perform(put("/api/v1/products/p-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /api/v1/products/{id} returns 200")
    void deleteProduct_returns200() throws Exception {
        doNothing().when(productService).deleteProduct("p-1");

        mockMvc.perform(delete("/api/v1/products/p-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/products/{id}/reviews returns paged reviews")
    void getProductReviews_returns200() throws Exception {
        ReviewDto reviewDto = ReviewDto.builder()
                .id("r-1").userId("u-1").userName("Alice").rating(5)
                .productId("p-1").build();
        PagedResponse<ReviewDto> paged = PagedResponse.<ReviewDto>builder()
                .content(List.of(reviewDto)).totalItems(1).currentPage(0).pageSize(10)
                .build();
        when(productService.getProductReviews(eq("p-1"), any())).thenReturn(paged);

        mockMvc.perform(get("/api/v1/products/p-1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalItems").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/products/{id}/reviews returns 201 with created review")
    void addReview_returns201() throws Exception {
        ReviewCreateRequest req = ReviewCreateRequest.builder()
                .userId("u-2").userName("Bob").rating(4).title("Good").build();
        ReviewDto created = ReviewDto.builder().id("r-new").userId("u-2").rating(4).build();
        when(productService.addReview(eq("p-1"), any())).thenReturn(created);

        mockMvc.perform(post("/api/v1/products/p-1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.rating").value(4));
    }

    @Test
    @DisplayName("POST /api/v1/products/{id}/reviews returns 400 for missing userId")
    void addReview_missingUserId_returns400() throws Exception {
        ReviewCreateRequest req = ReviewCreateRequest.builder()
                .userName("Bob").rating(4).build(); // userId missing

        mockMvc.perform(post("/api/v1/products/p-1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
