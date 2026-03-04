package com.commerce4retail.product.controller;

import com.commerce4retail.product.dto.*;
import com.commerce4retail.product.exception.ResourceNotFoundException;
import com.commerce4retail.product.service.BrandService;
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

@WebMvcTest(BrandController.class)
@DisplayName("BrandController Tests")
class BrandControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean BrandService brandService;

    private BrandDto sampleBrand;

    @BeforeEach
    void setUp() {
        sampleBrand = BrandDto.builder()
                .id("b-1").name("Nike").slug("nike").isActive(true).build();
    }

    @Test
    @DisplayName("GET /api/v1/brands returns 200 with paged list")
    void getAllBrands_returns200() throws Exception {
        PagedResponse<BrandDto> paged = PagedResponse.<BrandDto>builder()
                .content(List.of(sampleBrand)).totalItems(1).currentPage(0).pageSize(20).build();
        when(brandService.getAllBrands(anyBoolean(), any())).thenReturn(paged);

        mockMvc.perform(get("/api/v1/brands").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Nike"));
    }

    @Test
    @DisplayName("GET /api/v1/brands/{id} returns 200 when found")
    void getBrandById_found_returns200() throws Exception {
        when(brandService.getBrandById("b-1")).thenReturn(sampleBrand);

        mockMvc.perform(get("/api/v1/brands/b-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.slug").value("nike"));
    }

    @Test
    @DisplayName("GET /api/v1/brands/{id} returns 404 when not found")
    void getBrandById_notFound_returns404() throws Exception {
        when(brandService.getBrandById("bad")).thenThrow(new ResourceNotFoundException("Brand not found with id: bad"));

        mockMvc.perform(get("/api/v1/brands/bad"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/brands returns 201 with created brand")
    void createBrand_returns201() throws Exception {
        BrandDto req = BrandDto.builder().name("Adidas").slug("adidas").build();
        when(brandService.createBrand(any())).thenReturn(sampleBrand);

        mockMvc.perform(post("/api/v1/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/brands returns 400 when name is blank")
    void createBrand_blankName_returns400() throws Exception {
        BrandDto req = BrandDto.builder().name("").slug("adidas").build();

        mockMvc.perform(post("/api/v1/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/brands/{id} returns 200 with updated brand")
    void updateBrand_returns200() throws Exception {
        when(brandService.updateBrand(eq("b-1"), any())).thenReturn(sampleBrand);

        mockMvc.perform(put("/api/v1/brands/b-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleBrand)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/brands/{id} returns 200")
    void deleteBrand_returns200() throws Exception {
        doNothing().when(brandService).deleteBrand("b-1");

        mockMvc.perform(delete("/api/v1/brands/b-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
