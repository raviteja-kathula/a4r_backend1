package com.commerce4retail.cart.repository;

import com.commerce4retail.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, String> {

    Optional<CartItem> findByIdAndCartUserId(String itemId, String userId);

    Optional<CartItem> findByCartIdAndProductIdAndSizeAndColor(
            String cartId, String productId, String size, String color);
}
