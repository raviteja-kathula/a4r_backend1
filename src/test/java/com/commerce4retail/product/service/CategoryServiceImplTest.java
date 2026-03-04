package com.commerce4retail.product.service;

import com.commerce4retail.product.dto.CategoryDto;
import com.commerce4retail.product.dto.PagedResponse;
import com.commerce4retail.product.entity.Category;
import com.commerce4retail.product.exception.BadRequestException;
import com.commerce4retail.product.exception.ResourceNotFoundException;
import com.commerce4retail.product.repository.CategoryRepository;
import com.commerce4retail.product.service.impl.CategoryServiceImpl;
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
@DisplayName("CategoryServiceImpl Tests")
class CategoryServiceImplTest {

    @Mock private CategoryRepository categoryRepository;
    @InjectMocks private CategoryServiceImpl categoryService;

    private Category sampleCategory;

    @BeforeEach
    void setUp() {
        sampleCategory = Category.builder()
                .id("cat-1").name("Shirts").slug("shirts").isActive(true)
                .displayOrder(1).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("getAllCategories returns paged response")
    void getAllCategories_paged() {
        Page<Category> page = new PageImpl<>(List.of(sampleCategory));
        when(categoryRepository.findAll(any(Pageable.class))).thenReturn(page);

        PagedResponse<CategoryDto> result = categoryService.getAllCategories(false, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Shirts");
    }

    @Test
    @DisplayName("getAllCategories activeOnly uses findByIsActiveTrue")
    void getAllCategories_activeOnly() {
        Page<Category> page = new PageImpl<>(List.of(sampleCategory));
        when(categoryRepository.findByIsActiveTrue(any())).thenReturn(page);

        categoryService.getAllCategories(true, PageRequest.of(0, 20));

        verify(categoryRepository).findByIsActiveTrue(any());
    }

    @Test
    @DisplayName("getRootCategories returns root categories only")
    void getRootCategories_success() {
        when(categoryRepository.findByParentIsNullAndIsActiveTrueOrderByDisplayOrderAsc())
                .thenReturn(List.of(sampleCategory));

        List<CategoryDto> result = categoryService.getRootCategories();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSlug()).isEqualTo("shirts");
    }

    @Test
    @DisplayName("getCategoryById returns CategoryDto")
    void getCategoryById_found() {
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(sampleCategory));
        CategoryDto dto = categoryService.getCategoryById("cat-1");
        assertThat(dto.getId()).isEqualTo("cat-1");
    }

    @Test
    @DisplayName("getCategoryById throws when not found")
    void getCategoryById_notFound_throws() {
        when(categoryRepository.findById("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> categoryService.getCategoryById("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getCategoryBySlug returns CategoryDto")
    void getCategoryBySlug_found() {
        when(categoryRepository.findBySlug("shirts")).thenReturn(Optional.of(sampleCategory));
        CategoryDto dto = categoryService.getCategoryBySlug("shirts");
        assertThat(dto.getSlug()).isEqualTo("shirts");
    }

    @Test
    @DisplayName("getCategoryBySlug throws when slug not found")
    void getCategoryBySlug_notFound_throws() {
        when(categoryRepository.findBySlug("no-slug")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> categoryService.getCategoryBySlug("no-slug"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("createCategory persists and returns CategoryDto")
    void createCategory_success() {
        CategoryDto req = CategoryDto.builder().name("Denim").slug("denim").isActive(true).build();
        when(categoryRepository.existsBySlug("denim")).thenReturn(false);
        when(categoryRepository.save(any())).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId("cat-new");
            c.setCreatedAt(LocalDateTime.now());
            c.setUpdatedAt(LocalDateTime.now());
            return c;
        });

        CategoryDto result = categoryService.createCategory(req);

        assertThat(result.getName()).isEqualTo("Denim");
        verify(categoryRepository).save(any());
    }

    @Test
    @DisplayName("createCategory throws when slug exists")
    void createCategory_duplicateSlug_throws() {
        CategoryDto req = CategoryDto.builder().name("Shirts").slug("shirts").build();
        when(categoryRepository.existsBySlug("shirts")).thenReturn(true);
        assertThatThrownBy(() -> categoryService.createCategory(req))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("updateCategory updates name and persists")
    void updateCategory_success() {
        CategoryDto req = CategoryDto.builder().name("Updated Shirts").build();
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(sampleCategory));
        when(categoryRepository.save(any())).thenReturn(sampleCategory);

        CategoryDto result = categoryService.updateCategory("cat-1", req);
        assertThat(result).isNotNull();
        verify(categoryRepository).save(any());
    }

    @Test
    @DisplayName("updateCategory throws on slug conflict")
    void updateCategory_slugConflict_throws() {
        CategoryDto req = CategoryDto.builder().slug("taken").build();
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(sampleCategory));
        when(categoryRepository.existsBySlugAndIdNot("taken", "cat-1")).thenReturn(true);
        assertThatThrownBy(() -> categoryService.updateCategory("cat-1", req))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("deleteCategory removes entity")
    void deleteCategory_success() {
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(sampleCategory));
        categoryService.deleteCategory("cat-1");
        verify(categoryRepository).delete(sampleCategory);
    }

    @Test
    @DisplayName("deleteCategory throws when not found")
    void deleteCategory_notFound_throws() {
        when(categoryRepository.findById("nope")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> categoryService.deleteCategory("nope"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
