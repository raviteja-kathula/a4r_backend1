package com.commerce4retail.cart.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cart and CartItem Entity Tests")
class CartEntityTest {

    @Test
    @DisplayName("Cart.prePersist sets createdAt and updatedAt")
    void cart_prePersist_setsTimestamps() {
        Cart cart = Cart.builder()
                .userId("user-1")
                .items(new ArrayList<>())
                .build();

        cart.prePersist();

        assertThat(cart.getCreatedAt()).isNotNull();
        assertThat(cart.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Cart.preUpdate updates updatedAt only")
    void cart_preUpdate_updatesTimestamp() throws InterruptedException {
        Cart cart = Cart.builder()
                .userId("user-1")
                .items(new ArrayList<>())
                .build();
        cart.prePersist();
        var firstUpdated = cart.getUpdatedAt();
        Thread.sleep(5);

        cart.preUpdate();

        assertThat(cart.getUpdatedAt()).isAfterOrEqualTo(firstUpdated);
        assertThat(cart.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("CartItem.prePersist sets createdAt and updatedAt")
    void cartItem_prePersist_setsTimestamps() {
        CartItem item = CartItem.builder()
                .productId("prod-1")
                .title("Test Product")
                .price(new BigDecimal("99.99"))
                .quantity(1)
                .size("M")
                .build();

        item.prePersist();

        assertThat(item.getCreatedAt()).isNotNull();
        assertThat(item.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("CartItem.preUpdate updates updatedAt only")
    void cartItem_preUpdate_updatesTimestamp() throws InterruptedException {
        CartItem item = CartItem.builder()
                .productId("prod-1")
                .title("Test Product")
                .price(new BigDecimal("99.99"))
                .quantity(1)
                .size("M")
                .build();
        item.prePersist();
        var firstUpdated = item.getUpdatedAt();
        Thread.sleep(5);

        item.preUpdate();

        assertThat(item.getUpdatedAt()).isAfterOrEqualTo(firstUpdated);
        assertThat(item.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Cart builder creates cart with expected values")
    void cart_builder_setsFields() {
        Cart cart = Cart.builder()
                .userId("u-99")
                .items(new ArrayList<>())
                .build();

        assertThat(cart.getUserId()).isEqualTo("u-99");
        assertThat(cart.getItems()).isEmpty();
        assertThat(cart.getId()).isNull();
    }

    @Test
    @DisplayName("CartItem builder creates item with expected values")
    void cartItem_builder_setsFields() {
        CartItem item = CartItem.builder()
                .productId("p-1")
                .title("Blazer")
                .image("/images/blazer.jpg")
                .price(new BigDecimal("150.00"))
                .quantity(3)
                .size("L")
                .color("black")
                .build();

        assertThat(item.getProductId()).isEqualTo("p-1");
        assertThat(item.getTitle()).isEqualTo("Blazer");
        assertThat(item.getImage()).isEqualTo("/images/blazer.jpg");
        assertThat(item.getPrice()).isEqualByComparingTo("150.00");
        assertThat(item.getQuantity()).isEqualTo(3);
        assertThat(item.getSize()).isEqualTo("L");
        assertThat(item.getColor()).isEqualTo("black");
    }
}
