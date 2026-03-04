package com.commerce4retail.product.service;

import com.commerce4retail.product.dto.BrandDto;
import com.commerce4retail.product.dto.PagedResponse;
import com.commerce4retail.product.entity.Brand;
import com.commerce4retail.product.exception.BadRequestException;
import com.commerce4retail.product.exception.ResourceNotFoundException;
import com.commerce4retail.product.repository.BrandRepository;
import com.commerce4retail.product.service.impl.BrandServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BrandServiceImpl Tests")
class BrandServiceImplTest {

    @Mock private BrandRepository brandRepository;
    @InjectMocks private BrandServiceImpl brandService;

    private Brand sampleBrand;

    @BeforeEach
    void setUp() {
        sampleBrand = Brand.builder()
                .id("b-1").name("Nike").slug("nike").isActive(true)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("getAllBrands returns paged response")
    void getAllBrands_paged() {
        Page<Brand> page = new PageImpl<>(List.of(sampleBrand), PageRequest.of(0, 20), 1);
        when(brandRepository.findAll(any(Pageable.class))).thenReturn(page);

        PagedResponse<BrandDto> result = brandService.getAllBrands(false, PageRequest.of(0, 20));

        assertThat(result.getTotalItems()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Nike");
    }

    @Test
    @DisplayName("getAllBrands with activeOnly uses findByIsActiveTrue")
    void getAllBrands_activeOnly() {
        Page<Brand> page = new PageImpl<>(List.of(sampleBrand));
        when(brandRepository.findByIsActiveTrue(any())).thenReturn(page);

        brandService.getAllBrands(true, PageRequest.of(0, 20));

        verify(brandRepository).findByIsActiveTrue(any());
        verify(brandRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("getBrandById returns BrandDto")
    void getBrandById_found() {
        when(brandRepository.findById("b-1")).thenReturn(Optional.of(sampleBrand));
        BrandDto dto = brandService.getBrandById("b-1");
        assertThat(dto.getId()).isEqualTo("b-1");
        assertThat(dto.getSlug()).isEqualTo("nike");
    }

    @Test
    @DisplayName("getBrandById throws when not found")
    void getBrandById_notFound_throws() {
        when(brandRepository.findById("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> brandService.getBrandById("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("createBrand persists and returns BrandDto")
    void createBrand_success() {
        BrandDto req = BrandDto.builder().name("Adidas").slug("adidas").isActive(true).build();
        when(brandRepository.existsBySlug("adidas")).thenReturn(false);
        when(brandRepository.save(any())).thenAnswer(inv -> {
            Brand b = inv.getArgument(0);
            b.setId("b-new");
            b.setCreatedAt(LocalDateTime.now());
            b.setUpdatedAt(LocalDateTime.now());
            return b;
        });

        BrandDto result = brandService.createBrand(req);

        assertThat(result.getName()).isEqualTo("Adidas");
        verify(brandRepository).save(any());
    }

    @Test
    @DisplayName("createBrand throws when slug exists")
    void createBrand_duplicateSlug_throws() {
        BrandDto req = BrandDto.builder().name("Nike").slug("nike").build();
        when(brandRepository.existsBySlug("nike")).thenReturn(true);
        assertThatThrownBy(() -> brandService.createBrand(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("updateBrand updates fields and persists")
    void updateBrand_success() {
        BrandDto req = BrandDto.builder().name("Nike Updated").build();
        when(brandRepository.findById("b-1")).thenReturn(Optional.of(sampleBrand));
        when(brandRepository.save(any())).thenReturn(sampleBrand);

        BrandDto result = brandService.updateBrand("b-1", req);
        assertThat(result).isNotNull();
        verify(brandRepository).save(any());
    }

    @Test
    @DisplayName("updateBrand throws when slug conflict")
    void updateBrand_slugConflict_throws() {
        BrandDto req = BrandDto.builder().slug("taken").build();
        when(brandRepository.findById("b-1")).thenReturn(Optional.of(sampleBrand));
        when(brandRepository.existsBySlugAndIdNot("taken", "b-1")).thenReturn(true);
        assertThatThrownBy(() -> brandService.updateBrand("b-1", req))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("deleteBrand removes entity")
    void deleteBrand_success() {
        when(brandRepository.findById("b-1")).thenReturn(Optional.of(sampleBrand));
        brandService.deleteBrand("b-1");
        verify(brandRepository).delete(sampleBrand);
    }

    @Test
    @DisplayName("deleteBrand throws when not found")
    void deleteBrand_notFound_throws() {
        when(brandRepository.findById("nope")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> brandService.deleteBrand("nope"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
