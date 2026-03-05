package com.commerce4retail.cart.controller;

import com.commerce4retail.cart.dto.AddCartItemRequest;
import com.commerce4retail.cart.dto.CartDto;
import com.commerce4retail.cart.dto.UpdateCartItemRequest;
import com.commerce4retail.cart.service.CartService;
import com.commerce4retail.product.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart management endpoints")
public class CartController {

    /**
     * Header used to identify the authenticated user.
     * In a production environment this would be resolved from the JWT principal;
     * here it is supplied by the API gateway / test client.
     */
    static final String USER_ID_HEADER = "X-User-Id";

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get the authenticated user's shopping cart")
    public ResponseEntity<ApiResponse<CartDto>> getCart(
            @Parameter(description = "Authenticated user id", required = true)
            @RequestHeader(USER_ID_HEADER) String userId) {

        CartDto cart = cartService.getCart(userId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @PostMapping("/items")
    @Operation(summary = "Add an item to the cart")
    public ResponseEntity<ApiResponse<CartDto>> addCartItem(
            @RequestHeader(USER_ID_HEADER) String userId,
            @Valid @RequestBody AddCartItemRequest request) {

        CartDto cart = cartService.addCartItem(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(cart));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update the quantity of a cart item")
    public ResponseEntity<ApiResponse<CartDto>> updateCartItem(
            @RequestHeader(USER_ID_HEADER) String userId,
            @PathVariable String itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {

        CartDto cart = cartService.updateCartItem(userId, itemId, request);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove an item from the cart")
    public ResponseEntity<ApiResponse<CartDto>> removeCartItem(
            @RequestHeader(USER_ID_HEADER) String userId,
            @PathVariable String itemId) {

        CartDto cart = cartService.removeCartItem(userId, itemId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }
}
