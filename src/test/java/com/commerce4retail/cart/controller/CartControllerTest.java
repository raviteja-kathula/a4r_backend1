package com.commerce4retail.cart.controller;

import com.commerce4retail.cart.dto.AddCartItemRequest;
import com.commerce4retail.cart.dto.CartDto;
import com.commerce4retail.cart.dto.CartItemDto;
import com.commerce4retail.cart.dto.UpdateCartItemRequest;
import com.commerce4retail.cart.service.CartService;
import com.commerce4retail.product.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@DisplayName("CartController Tests")
class CartControllerTest {

    private static final String USER_ID   = "user-123";
    private static final String CART_ID   = "cart-001";
    private static final String ITEM_ID   = "item-001";
    private static final String HEADER    = CartController.USER_ID_HEADER;

    @Autowired MockMvc     mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean  CartService cartService;

    private CartItemDto sampleItem;
    private CartDto     sampleCart;

    @BeforeEach
    void setUp() {
        sampleItem = CartItemDto.builder()
                .id(ITEM_ID).productId("prod-001").title("Classic Blazer")
                .price(new BigDecimal("285.00")).quantity(2)
                .size("M").color("navy").subtotal(new BigDecimal("570.00"))
                .build();

        sampleCart = CartDto.builder()
                .id(CART_ID).userId(USER_ID)
                .items(List.of(sampleItem))
                .subtotal(new BigDecimal("570.00"))
                .tax(new BigDecimal("51.30"))
                .shipping(new BigDecimal("15.00"))
                .total(new BigDecimal("636.30"))
                .itemCount(2)
                .build();
    }

    // ─── GET /api/cart ────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/cart returns 200 with cart")
    void getCart_returns200() throws Exception {
        when(cartService.getCart(USER_ID)).thenReturn(sampleCart);

        mockMvc.perform(get("/api/cart")
                        .header(HEADER, USER_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(CART_ID))
                .andExpect(jsonPath("$.data.userId").value(USER_ID))
                .andExpect(jsonPath("$.data.itemCount").value(2))
                .andExpect(jsonPath("$.data.total").value(636.30))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].title").value("Classic Blazer"));
    }

    @Test
    @DisplayName("GET /api/cart returns 400 when X-User-Id header missing")
    void getCart_missingHeader_returns400() throws Exception {
        mockMvc.perform(get("/api/cart").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ─── POST /api/cart/items ─────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/cart/items returns 201 with updated cart")
    void addCartItem_returns201() throws Exception {
        AddCartItemRequest request = new AddCartItemRequest("prod-001", 2, "M", "navy");
        when(cartService.addCartItem(eq(USER_ID), any(AddCartItemRequest.class)))
                .thenReturn(sampleCart);

        mockMvc.perform(post("/api/cart/items")
                        .header(HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(CART_ID))
                .andExpect(jsonPath("$.data.items", hasSize(1)));
    }

    @Test
    @DisplayName("POST /api/cart/items returns 400 when productId missing")
    void addCartItem_missingProductId_returns400() throws Exception {
        AddCartItemRequest bad = new AddCartItemRequest(null, 1, "M", null);

        mockMvc.perform(post("/api/cart/items")
                        .header(HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/cart/items returns 400 when quantity is 0")
    void addCartItem_zeroQuantity_returns400() throws Exception {
        AddCartItemRequest bad = new AddCartItemRequest("prod-001", 0, "M", null);

        mockMvc.perform(post("/api/cart/items")
                        .header(HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/cart/items returns 404 when product not found")
    void addCartItem_productNotFound_returns404() throws Exception {
        AddCartItemRequest request = new AddCartItemRequest("bad-id", 1, "M", null);
        when(cartService.addCartItem(eq(USER_ID), any()))
                .thenThrow(new ResourceNotFoundException("Product not found: bad-id"));

        mockMvc.perform(post("/api/cart/items")
                        .header(HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ─── PUT /api/cart/items/{itemId} ─────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/cart/items/{itemId} returns 200 with updated cart")
    void updateCartItem_returns200() throws Exception {
        UpdateCartItemRequest request = new UpdateCartItemRequest(3);
        when(cartService.updateCartItem(eq(USER_ID), eq(ITEM_ID), any()))
                .thenReturn(sampleCart);

        mockMvc.perform(put("/api/cart/items/{itemId}", ITEM_ID)
                        .header(HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(CART_ID));
    }

    @Test
    @DisplayName("PUT /api/cart/items/{itemId} returns 400 when quantity < 1")
    void updateCartItem_invalidQuantity_returns400() throws Exception {
        UpdateCartItemRequest bad = new UpdateCartItemRequest(0);

        mockMvc.perform(put("/api/cart/items/{itemId}", ITEM_ID)
                        .header(HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/cart/items/{itemId} returns 404 when item not found")
    void updateCartItem_notFound_returns404() throws Exception {
        when(cartService.updateCartItem(any(), eq("bad-item"), any()))
                .thenThrow(new ResourceNotFoundException("Cart item not found: bad-item"));

        mockMvc.perform(put("/api/cart/items/{itemId}", "bad-item")
                        .header(HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateCartItemRequest(1))))
                .andExpect(status().isNotFound());
    }

    // ─── DELETE /api/cart/items/{itemId} ─────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/cart/items/{itemId} returns 200 with updated cart")
    void removeCartItem_returns200() throws Exception {
        CartDto emptyCart = CartDto.builder().id(CART_ID).userId(USER_ID)
                .items(List.of()).subtotal(BigDecimal.ZERO).tax(BigDecimal.ZERO)
                .shipping(BigDecimal.ZERO).total(BigDecimal.ZERO).itemCount(0).build();
        when(cartService.removeCartItem(USER_ID, ITEM_ID)).thenReturn(emptyCart);

        mockMvc.perform(delete("/api/cart/items/{itemId}", ITEM_ID)
                        .header(HEADER, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.itemCount").value(0));
    }

    @Test
    @DisplayName("DELETE /api/cart/items/{itemId} returns 404 when item not found")
    void removeCartItem_notFound_returns404() throws Exception {
        when(cartService.removeCartItem(USER_ID, "bad-item"))
                .thenThrow(new ResourceNotFoundException("Cart item not found: bad-item"));

        mockMvc.perform(delete("/api/cart/items/{itemId}", "bad-item")
                        .header(HEADER, USER_ID))
                .andExpect(status().isNotFound());
    }
}
