package com.mjs.ecommerce.service;

import com.mjs.ecommerce.constants.Constants;
import com.mjs.ecommerce.model.*;
import com.mjs.ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private UserRepository userRepo;

    // =========================
    // ADD TO CART
    // =========================
    @Override
    public Cart addToCart(String username, Long productId, int quantity) {

        // Validate quantity
        validateQuantity(quantity);

        // Get user
        User user = userRepo.findByEmail(username)
                .orElseThrow(() -> new RuntimeException(Constants.USER_NOT_FOUND));

        // Get and validate product
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException(Constants.PRODUCT_NOT_FOUND));

        // Get or create cart
        Cart cart = getOrCreateCart(user);

        // Null-safe initialize items if null
        if (Optional.ofNullable(cart.getItems())
                .orElseGet(Collections::emptyList)
                .isEmpty()) {
            cart.setItems(new ArrayList<>());
        }

        // Null-safe find existing item
        Optional<CartItem> existingItem = Optional.ofNullable(cart.getItems())
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        // Calculate new quantity
        int newQuantity = existingItem
                .map(item -> item.getQuantity() + quantity)
                .orElse(quantity);

        // Validate stock before updating
        validateProductStock(product, newQuantity);

        // Update or add item
        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(newQuantity);
        } else {
            CartItem cartItem = new CartItem(cart, product, quantity, product.getPrice());
            Optional.ofNullable(cart.getItems())
                    .orElseGet(Collections::emptyList)
                    .add(cartItem);
        }

        return cartRepo.save(cart);
    }

    // =========================
    // GET CART
    // =========================
    @Override
    public Cart getCart(Long userId) {
        return cartRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException(Constants.CART_NOT_FOUND));
    }

    @Override
    public Cart getCartByUsername(String username) {
        User user = userRepo.findByEmail(username)
                .orElseThrow(() -> new RuntimeException(Constants.USER_NOT_FOUND));

        return cartRepo.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException(Constants.CART_NOT_FOUND));
    }

    // =========================
    // UPDATE QUANTITY
    // =========================

    /**
     * Update cart item quantity by userId
     * FIX #4: Removed code duplication - now calls common method
     */
    @Override
    public Cart updateQuantity(Long userId, Long productId, int quantity) {
        return getCart(userId, productId, quantity);
    }

    private Cart getCart(Long userId, Long productId, int quantity) {
        validateQuantity(quantity);
        Cart cart = cartRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException(Constants.CART_NOT_FOUND));
        updateCartItemQuantityWithValidation(cart, productId, quantity);
        return cartRepo.save(cart);
    }

    /**
     * Update cart item quantity by username
     * FIX #4: Removed code duplication - now calls common method
     */
    @Override
    public Cart updateQuantityByUsername(String username, Long productId, int quantity) {
        User user = userRepo.findByEmail(username)
                .orElseThrow(() -> new RuntimeException(Constants.USER_NOT_FOUND));
       return getCart(user.getId(), productId, quantity);
    }

    // =========================
    // REMOVE ITEM
    // =========================

    /**
     * Remove item from cart by username
     * FIX #3: Added validation to check if product exists before removal
     */
    @Override
    public Cart removeItemByUsername(String username, Long productId) {

        User user = userRepo.findByEmail(username)
                .orElseThrow(() -> new RuntimeException(Constants.USER_NOT_FOUND));

        Cart cart = cartRepo.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException(Constants.CART_NOT_FOUND));

        // Null-safe check if items list is null or empty
        if (Optional.ofNullable(cart.getItems())
                .orElseGet(Collections::emptyList)
                .isEmpty()) {
            throw new RuntimeException(Constants.CART_ITEM_IS_NULL);
        }

        // Null-safe validate product exists in cart before removal
        boolean itemExists = Optional.ofNullable(cart.getItems())
                .orElseGet(Collections::emptyList)
                .stream()
                .anyMatch(item -> item.getProduct().getId().equals(productId));

        if (!itemExists) {
            throw new RuntimeException(
                    "Product with ID " + productId + " not found in cart"
            );
        }

        // Null-safe remove the item
        Optional.ofNullable(cart.getItems())
                .orElseGet(Collections::emptyList)
                .removeIf(item -> item.getProduct().getId().equals(productId));

        return cartRepo.save(cart);
    }

    // =========================
    // PRIVATE HELPER METHODS
    // =========================

    /**
     * Validate quantity is positive
     */
    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
    }
   
    private void validateProductStock(Product product, int requestedQuantity) {
        if (product.getStockQuantity() < requestedQuantity) {
            throw new RuntimeException(
                    "Requested quantity exceeds available stock. Available: "
                            + product.getStockQuantity() + ", Requested: " + requestedQuantity
            );
        }
    }

    /**
     * Get cart for user, create if doesn't exist
     */
    private Cart getOrCreateCart(User user) {
        return cartRepo.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setItems(new ArrayList<>());
                    return cartRepo.save(newCart);
                });
    }

    /**
     * FIX #2 & #4: Update cart item quantity with stock validation
     * This method is called by both updateQuantity and updateQuantityByUsername
     * Eliminates code duplication and ensures consistent validation
     */
    private void updateCartItemQuantityWithValidation(Cart cart, Long productId, int newQuantity) {

        // Null-safe check if items list is null or empty
        if (Optional.ofNullable(cart.getItems())
                .orElseGet(Collections::emptyList)
                .isEmpty()) {
            throw new RuntimeException(Constants.PRODUCT_NOT_FOUND);
        }

        // Null-safe find the cart item
        CartItem itemToUpdate = Optional.ofNullable(cart.getItems())
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(Constants.PRODUCT_NOT_FOUND));

        // Validate stock before updating quantity
        Product product = itemToUpdate.getProduct();
        validateProductStock(product, newQuantity);

        // Update quantity
        itemToUpdate.setQuantity(newQuantity);
    }
    /**
     * Clear entire cart
     * Bonus: Useful utility method
     */
    public Cart clearCart(String username) {
        User user = userRepo.findByEmail(username)
                .orElseThrow(() -> new RuntimeException(Constants.USER_NOT_FOUND));

        Cart cart = cartRepo.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException(Constants.CART_NOT_FOUND));

        Optional.ofNullable(cart.getItems())
                .orElseGet(Collections::emptyList)
                .clear();

        return cartRepo.save(cart);
    }

    /**
     * Get cart total price
     * Bonus: Useful utility method
     */
    public double getCartTotal(String username) {
        Cart cart = getCartByUsername(username);

        return Optional.ofNullable(cart.getItems())
                .orElseGet(Collections::emptyList)
                .stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    /**
     * Get total items count in cart
     * Bonus: Useful utility method
     */
    public int getCartItemCount(String username) {
        Cart cart = getCartByUsername(username);

        return Optional.ofNullable(cart.getItems())
                .orElseGet(Collections::emptyList)
                .stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
}