package com.commerce4retail.product.controller;

import com.commerce4retail.product.dto.*;
import com.commerce4retail.product.exception.ResourceNotFoundException;
import com.commerce4retail.product.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@DisplayName("CategoryController Tests")
class CategoryControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean CategoryService categoryService;

    private CategoryDto sampleCategory;

    @BeforeEach
    void setUp() {
        sampleCategory = CategoryDto.builder()
                .id("c-1").name("Shirts").slug("shirts").isActive(true).displayOrder(1).build();
    }

    @Test
    @DisplayName("GET /api/v1/categories returns 200 with paged list")
    void getAllCategories_returns200() throws Exception {
        PagedResponse<CategoryDto> paged = PagedResponse.<CategoryDto>builder()
                .content(List.of(sampleCategory)).totalItems(1).currentPage(0).pageSize(20).build();
        when(categoryService.getAllCategories(anyBoolean(), any())).thenReturn(paged);

        mockMvc.perform(get("/api/v1/categories").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Shirts"));
    }

    @Test
    @DisplayName("GET /api/v1/categories/roots returns root categories")
    void getRootCategories_returns200() throws Exception {
        when(categoryService.getRootCategories()).thenReturn(List.of(sampleCategory));

        mockMvc.perform(get("/api/v1/categories/roots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].slug").value("shirts"));
    }

    @Test
    @DisplayName("GET /api/v1/categories/{id} returns 200 when found")
    void getCategoryById_found_returns200() throws Exception {
        when(categoryService.getCategoryById("c-1")).thenReturn(sampleCategory);

        mockMvc.perform(get("/api/v1/categories/c-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("c-1"));
    }

    @Test
    @DisplayName("GET /api/v1/categories/{id} returns 404 when not found")
    void getCategoryById_notFound_returns404() throws Exception {
        when(categoryService.getCategoryById("bad"))
                .thenThrow(new ResourceNotFoundException("Category not found with id: bad"));

        mockMvc.perform(get("/api/v1/categories/bad"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/categories/slug/{slug} returns 200 when found")
    void getCategoryBySlug_returns200() throws Exception {
        when(categoryService.getCategoryBySlug("shirts")).thenReturn(sampleCategory);

        mockMvc.perform(get("/api/v1/categories/slug/shirts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.slug").value("shirts"));
    }

    @Test
    @DisplayName("POST /api/v1/categories returns 201")
    void createCategory_returns201() throws Exception {
        CategoryDto req = CategoryDto.builder().name("Denim").slug("denim").build();
        when(categoryService.createCategory(any())).thenReturn(sampleCategory);

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/categories returns 400 for blank name")
    void createCategory_blankName_returns400() throws Exception {
        CategoryDto req = CategoryDto.builder().name("").slug("valid-slug").build();

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/categories/{id} returns 200")
    void updateCategory_returns200() throws Exception {
        when(categoryService.updateCategory(eq("c-1"), any())).thenReturn(sampleCategory);

        mockMvc.perform(put("/api/v1/categories/c-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCategory)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} returns 200")
    void deleteCategory_returns200() throws Exception {
        doNothing().when(categoryService).deleteCategory("c-1");

        mockMvc.perform(delete("/api/v1/categories/c-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
