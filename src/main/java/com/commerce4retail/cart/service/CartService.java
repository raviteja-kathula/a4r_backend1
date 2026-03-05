package com.commerce4retail.cart.service;

import com.commerce4retail.cart.dto.AddCartItemRequest;
import com.commerce4retail.cart.dto.CartDto;
import com.commerce4retail.cart.dto.UpdateCartItemRequest;

public interface CartService {

    /**
     * Retrieve the cart for the given user (creates one if it doesn't exist).
     */
    CartDto getCart(String userId);

    /**
     * Add an item to the user's cart. If a matching product+size+color already
     * exists, the quantities are merged.
     */
    CartDto addCartItem(String userId, AddCartItemRequest request);

    /**
     * Update the quantity of an existing cart item.
     */
    CartDto updateCartItem(String userId, String itemId, UpdateCartItemRequest request);

    /**
     * Remove an item from the user's cart.
     */
    CartDto removeCartItem(String userId, String itemId);
}
