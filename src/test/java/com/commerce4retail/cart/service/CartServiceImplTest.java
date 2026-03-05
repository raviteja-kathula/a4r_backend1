package com.commerce4retail.cart.service;

import com.commerce4retail.cart.dto.AddCartItemRequest;
import com.commerce4retail.cart.dto.CartDto;
import com.commerce4retail.cart.dto.UpdateCartItemRequest;
import com.commerce4retail.cart.entity.Cart;
import com.commerce4retail.cart.entity.CartItem;
import com.commerce4retail.cart.repository.CartItemRepository;
import com.commerce4retail.cart.repository.CartRepository;
import com.commerce4retail.cart.service.impl.CartServiceImpl;
import com.commerce4retail.product.entity.Product;
import com.commerce4retail.product.entity.ProductImage;
import com.commerce4retail.product.exception.ResourceNotFoundException;
import com.commerce4retail.product.repository.ProductImageRepository;
import com.commerce4retail.product.repository.ProductRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartServiceImpl Tests")
class CartServiceImplTest {

    @Mock private CartRepository         cartRepository;
    @Mock private CartItemRepository     cartItemRepository;
    @Mock private ProductRepository      productRepository;
    @Mock private ProductImageRepository productImageRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private static final String USER_ID    = "user-123";
    private static final String CART_ID    = "cart-001";
    private static final String PRODUCT_ID = "prod-001";
    private static final String ITEM_ID    = "item-001";

    private Product sampleProduct;
    private Cart    sampleCart;
    private CartItem sampleCartItem;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(PRODUCT_ID)
                .name("Classic Blazer")
                .basePrice(new BigDecimal("285.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleCartItem = CartItem.builder()
                .id(ITEM_ID)
                .productId(PRODUCT_ID)
                .title("Classic Blazer")
                .price(new BigDecimal("285.00"))
                .quantity(2)
                .size("M")
                .color("navy")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleCart = Cart.builder()
                .id(CART_ID)
                .userId(USER_ID)
                .items(new ArrayList<>(List.of(sampleCartItem)))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleCartItem.setCart(sampleCart);
    }

    // ─── getCart ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getCart")
    class GetCart {

        @Test
        @DisplayName("returns CartDto for existing cart")
        void getCart_existingCart_returnsDto() {
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(sampleCart));

            CartDto result = cartService.getCart(USER_ID);

            assertThat(result.getId()).isEqualTo(CART_ID);
            assertThat(result.getUserId()).isEqualTo(USER_ID);
            assertThat(result.getItems()).hasSize(1);
            assertThat(result.getItemCount()).isEqualTo(2);
            assertThat(result.getSubtotal()).isEqualByComparingTo("570.00");
            assertThat(result.getTax()).isEqualByComparingTo("51.30");
            assertThat(result.getShipping()).isEqualByComparingTo("15.00");
            assertThat(result.getTotal()).isEqualByComparingTo("636.30");
        }

        @Test
        @DisplayName("returns empty CartDto when no cart found (no persistence side-effect on GET)")
        void getCart_noCart_returnsEmptyDto() {
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

            CartDto result = cartService.getCart(USER_ID);

            assertThat(result.getUserId()).isEqualTo(USER_ID);
            assertThat(result.getItems()).isEmpty();
            assertThat(result.getItemCount()).isZero();
            assertThat(result.getSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);
            verify(cartRepository, never()).save(any());
        }
    }

    // ─── addCartItem ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("addCartItem")
    class AddCartItem {

        @Test
        @DisplayName("creates new cart and adds item when no cart exists")
        void addCartItem_noExistingCart_createsCartAndItem() {
            AddCartItemRequest request = new AddCartItemRequest(PRODUCT_ID, 1, "M", "navy");

            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
            Cart newCart = Cart.builder().id(CART_ID).userId(USER_ID)
                    .items(new ArrayList<>()).createdAt(LocalDateTime.now()).build();
            when(cartRepository.save(any(Cart.class))).thenReturn(newCart);
            when(productRepository.findByIdAndDeletedAtIsNull(PRODUCT_ID))
                    .thenReturn(Optional.of(sampleProduct));
            when(cartItemRepository.findByCartIdAndProductIdAndSizeAndColor(
                    CART_ID, PRODUCT_ID, "M", "navy")).thenReturn(Optional.empty());
            when(productImageRepository.findByProductIdAndIsPrimaryTrue(PRODUCT_ID))
                    .thenReturn(Optional.empty());

            CartDto result = cartService.addCartItem(USER_ID, request);

            assertThat(result).isNotNull();
            verify(cartRepository, atLeastOnce()).save(any(Cart.class));
        }

        @Test
        @DisplayName("merges quantities when same product+size+color already in cart")
        void addCartItem_existingMatchingItem_mergesQuantity() {
            AddCartItemRequest request = new AddCartItemRequest(PRODUCT_ID, 1, "M", "navy");

            Cart existingCart = Cart.builder().id(CART_ID).userId(USER_ID)
                    .items(new ArrayList<>()).createdAt(LocalDateTime.now()).build();
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existingCart));
            when(productRepository.findByIdAndDeletedAtIsNull(PRODUCT_ID))
                    .thenReturn(Optional.of(sampleProduct));
            when(cartItemRepository.findByCartIdAndProductIdAndSizeAndColor(
                    CART_ID, PRODUCT_ID, "M", "navy"))
                    .thenReturn(Optional.of(sampleCartItem));
            when(cartItemRepository.save(any())).thenReturn(sampleCartItem);
            when(cartRepository.save(any())).thenReturn(existingCart);

            cartService.addCartItem(USER_ID, request);

            assertThat(sampleCartItem.getQuantity()).isEqualTo(3); // 2 + 1
            verify(cartItemRepository).save(sampleCartItem);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when product not found")
        void addCartItem_productNotFound_throws() {
            AddCartItemRequest request = new AddCartItemRequest("bad-id", 1, "M", null);

            Cart existingCart = Cart.builder().id(CART_ID).userId(USER_ID)
                    .items(new ArrayList<>()).createdAt(LocalDateTime.now()).build();
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existingCart));
            when(productRepository.findByIdAndDeletedAtIsNull("bad-id"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.addCartItem(USER_ID, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("bad-id");
        }

        @Test
        @DisplayName("adds item image from primary product image when available")
        void addCartItem_withPrimaryImage_setsImageOnItem() {
            AddCartItemRequest request = new AddCartItemRequest(PRODUCT_ID, 1, "L", "black");
            Cart existingCart = Cart.builder().id(CART_ID).userId(USER_ID)
                    .items(new ArrayList<>()).createdAt(LocalDateTime.now()).build();

            ProductImage img = ProductImage.builder()
                    .id("img-1").url("/images/blazer.jpg").isPrimary(true).build();

            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existingCart));
            when(productRepository.findByIdAndDeletedAtIsNull(PRODUCT_ID))
                    .thenReturn(Optional.of(sampleProduct));
            when(cartItemRepository.findByCartIdAndProductIdAndSizeAndColor(
                    CART_ID, PRODUCT_ID, "L", "black")).thenReturn(Optional.empty());
            when(productImageRepository.findByProductIdAndIsPrimaryTrue(PRODUCT_ID))
                    .thenReturn(Optional.of(img));

            ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
            when(cartRepository.save(cartCaptor.capture())).thenAnswer(i -> i.getArgument(0));

            cartService.addCartItem(USER_ID, request);

            Cart saved = cartCaptor.getValue();
            assertThat(saved.getItems()).hasSize(1);
            assertThat(saved.getItems().get(0).getImage()).isEqualTo("/images/blazer.jpg");
        }
    }

    // ─── updateCartItem ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateCartItem")
    class UpdateCartItem {

        @Test
        @DisplayName("updates quantity and returns refreshed CartDto")
        void updateCartItem_validItem_updatesQty() {
            UpdateCartItemRequest request = new UpdateCartItemRequest(5);

            when(cartItemRepository.findByIdAndCartUserId(ITEM_ID, USER_ID))
                    .thenReturn(Optional.of(sampleCartItem));
            when(cartItemRepository.save(any())).thenReturn(sampleCartItem);
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(sampleCart));

            CartDto result = cartService.updateCartItem(USER_ID, ITEM_ID, request);

            assertThat(sampleCartItem.getQuantity()).isEqualTo(5);
            assertThat(result.getUserId()).isEqualTo(USER_ID);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when item not found for user")
        void updateCartItem_notFound_throws() {
            when(cartItemRepository.findByIdAndCartUserId(ITEM_ID, USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    cartService.updateCartItem(USER_ID, ITEM_ID, new UpdateCartItemRequest(2)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(ITEM_ID);
        }
    }

    // ─── removeCartItem ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("removeCartItem")
    class RemoveCartItem {

        @Test
        @DisplayName("removes item and returns updated CartDto")
        void removeCartItem_validItem_removesAndReturnsCart() {
            Cart cartWithItem = Cart.builder()
                    .id(CART_ID).userId(USER_ID)
                    .items(new ArrayList<>(List.of(sampleCartItem)))
                    .createdAt(LocalDateTime.now()).build();
            sampleCartItem.setCart(cartWithItem);

            when(cartItemRepository.findByIdAndCartUserId(ITEM_ID, USER_ID))
                    .thenReturn(Optional.of(sampleCartItem));
            Cart emptyCart = Cart.builder().id(CART_ID).userId(USER_ID)
                    .items(new ArrayList<>()).createdAt(LocalDateTime.now()).build();
            when(cartRepository.save(any())).thenReturn(emptyCart);

            CartDto result = cartService.removeCartItem(USER_ID, ITEM_ID);

            verify(cartItemRepository).delete(sampleCartItem);
            assertThat(result.getItems()).isEmpty();
            assertThat(result.getItemCount()).isZero();
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when item not found for user")
        void removeCartItem_notFound_throws() {
            when(cartItemRepository.findByIdAndCartUserId("bad-item", USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.removeCartItem(USER_ID, "bad-item"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("bad-item");
        }
    }

    // ─── toDto pricing ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Pricing calculations")
    class PricingCalculations {

        @Test
        @DisplayName("empty cart has zero subtotal, tax, shipping and total")
        void emptyCart_zeroPricing() {
            Cart empty = Cart.builder().id("c").userId(USER_ID)
                    .items(new ArrayList<>()).createdAt(LocalDateTime.now()).build();
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(empty));

            CartDto dto = cartService.getCart(USER_ID);

            assertThat(dto.getSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(dto.getTax()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(dto.getShipping()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(dto.getTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("non-empty cart computes subtotal, 9% tax, $15 shipping, and total correctly")
        void nonEmptyCart_correctPricing() {
            // item: $285.00 x 2 = $570.00 subtotal
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(sampleCart));

            CartDto dto = cartService.getCart(USER_ID);

            assertThat(dto.getSubtotal()).isEqualByComparingTo("570.00");
            assertThat(dto.getTax()).isEqualByComparingTo("51.30");
            assertThat(dto.getShipping()).isEqualByComparingTo("15.00");
            assertThat(dto.getTotal()).isEqualByComparingTo("636.30");
        }
    }
}
