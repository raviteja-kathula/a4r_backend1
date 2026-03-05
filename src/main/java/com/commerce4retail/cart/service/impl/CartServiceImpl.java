package com.commerce4retail.cart.service.impl;

import com.commerce4retail.cart.dto.AddCartItemRequest;
import com.commerce4retail.cart.dto.CartDto;
import com.commerce4retail.cart.dto.CartItemDto;
import com.commerce4retail.cart.dto.UpdateCartItemRequest;
import com.commerce4retail.cart.entity.Cart;
import com.commerce4retail.cart.entity.CartItem;
import com.commerce4retail.cart.repository.CartItemRepository;
import com.commerce4retail.cart.repository.CartRepository;
import com.commerce4retail.cart.service.CartService;
import com.commerce4retail.product.entity.Product;
import com.commerce4retail.product.entity.ProductImage;
import com.commerce4retail.product.exception.ResourceNotFoundException;
import com.commerce4retail.product.repository.ProductImageRepository;
import com.commerce4retail.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CartServiceImpl implements CartService {

    private static final BigDecimal TAX_RATE    = new BigDecimal("0.09");
    private static final BigDecimal SHIPPING_FEE = new BigDecimal("15.00");

    private final CartRepository        cartRepository;
    private final CartItemRepository    cartItemRepository;
    private final ProductRepository     productRepository;
    private final ProductImageRepository productImageRepository;

    // ─── Public API ──────────────────────────────────────────────────────────

    @Override
    public CartDto getCart(String userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.debug("No cart for user {}; returning empty cart view", userId);
                    Cart empty = Cart.builder().userId(userId).build();
                    // Not persisted – just an in-memory projection so the caller
                    // always gets a valid response without side effects on GET.
                    return empty;
                });
        return toDto(cart);
    }

    @Override
    @Transactional
    public CartDto addCartItem(String userId, AddCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);

        Product product = productRepository.findByIdAndDeletedAtIsNull(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found: " + request.getProductId()));

        String color = request.getColor();

        // Merge quantities when the same product+size+color already exists.
        Optional<CartItem> existing = cartItemRepository
                .findByCartIdAndProductIdAndSizeAndColor(
                        cart.getId(), request.getProductId(), request.getSize(), color);

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepository.save(item);
            log.debug("Merged qty for item {} in cart {}", item.getId(), cart.getId());
        } else {
            String imageUrl = productImageRepository
                    .findByProductIdAndIsPrimaryTrue(product.getId())
                    .map(ProductImage::getUrl)
                    .orElse(null);

            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .productId(product.getId())
                    .title(product.getName())
                    .image(imageUrl)
                    .price(product.getBasePrice())
                    .quantity(request.getQuantity())
                    .size(request.getSize())
                    .color(color)
                    .build();
            cart.getItems().add(newItem);
            log.debug("Added new item for product {} to cart {}", product.getId(), cart.getId());
        }

        Cart saved = cartRepository.save(cart);
        return toDto(saved);
    }

    @Override
    @Transactional
    public CartDto updateCartItem(String userId, String itemId, UpdateCartItemRequest request) {
        CartItem item = cartItemRepository.findByIdAndCartUserId(itemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart item not found: " + itemId));

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        log.debug("Updated item {} qty to {}", itemId, request.getQuantity());

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));
        return toDto(cart);
    }

    @Override
    @Transactional
    public CartDto removeCartItem(String userId, String itemId) {
        CartItem item = cartItemRepository.findByIdAndCartUserId(itemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart item not found: " + itemId));

        Cart cart = item.getCart();
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        Cart saved = cartRepository.save(cart);
        log.debug("Removed item {} from cart {}", itemId, saved.getId());
        return toDto(saved);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Returns the persisted cart for the user, creating one if absent.
     */
    private Cart getOrCreateCart(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart cart = Cart.builder().userId(userId).build();
                    Cart saved = cartRepository.save(cart);
                    log.debug("Created new cart {} for user {}", saved.getId(), userId);
                    return saved;
                });
    }

    private CartDto toDto(Cart cart) {
        List<CartItemDto> itemDtos = cart.getItems() == null
                ? List.of()
                : cart.getItems().stream()
                        .map(this::toItemDto)
                        .collect(Collectors.toList());

        BigDecimal subtotal = itemDtos.stream()
                .map(CartItemDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        boolean hasItems = !itemDtos.isEmpty();
        BigDecimal tax      = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal shipping = hasItems ? SHIPPING_FEE : BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY);
        BigDecimal total    = subtotal.add(tax).add(shipping);

        int itemCount = itemDtos.stream().mapToInt(CartItemDto::getQuantity).sum();

        return CartDto.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .items(itemDtos)
                .subtotal(subtotal)
                .tax(tax)
                .shipping(shipping)
                .total(total)
                .itemCount(itemCount)
                .build();
    }

    private CartItemDto toItemDto(CartItem item) {
        BigDecimal lineSubtotal = item.getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);
        return CartItemDto.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .title(item.getTitle())
                .image(item.getImage())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .size(item.getSize())
                .color(item.getColor())
                .subtotal(lineSubtotal)
                .build();
    }
}
