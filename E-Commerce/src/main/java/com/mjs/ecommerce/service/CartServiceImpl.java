package com.mjs.ecommerce.service;

import com.mjs.ecommerce.constants.Constants;
import com.mjs.ecommerce.model.*;
import com.mjs.ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        User user = userRepo.findByEmail(username)
                .orElseThrow(() -> new RuntimeException(Constants.USER_NOT_FOUND));

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException(Constants.PRODUCT_NOT_FOUND));

        Cart cart = cartRepo.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setItems(new ArrayList<>());
                    return cartRepo.save(newCart);
                });

        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        int newQuantity = existingItem
                .map(item -> item.getQuantity() + quantity)
                .orElse(quantity);

        if (product.getStockQuantity() < newQuantity) {
            throw new RuntimeException(
                    "Requested quantity exceeds available stock. Available: " + product.getStockQuantity()
            );
        }

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(newQuantity);
        } else {
            CartItem cartItem = new CartItem(cart, product, quantity, product.getPrice());
            cart.getItems().add(cartItem);
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
    // UPDATE QUANTITY (COMMON LOGIC USED)
    // =========================
    @Override
    public Cart updateQuantity(Long userId, Long productId, int quantity) {

        validateQuantity(quantity);

        Cart cart = cartRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException(Constants.CART_NOT_FOUND));

        updateCartItemQuantity(cart, productId, quantity);

        return cartRepo.save(cart);
    }

    @Override
    public Cart updateQuantityByUsername(String username, Long productId, int quantity) {

        validateQuantity(quantity);

        User user = userRepo.findByEmail(username)
                .orElseThrow(() -> new RuntimeException(Constants.USER_NOT_FOUND));

        Cart cart = cartRepo.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException(Constants.CART_NOT_FOUND));

        updateCartItemQuantity(cart, productId, quantity);

        return cartRepo.save(cart);
    }

    // =========================
    // REMOVE ITEM
    // =========================
    @Override
    public Cart removeItemByUsername(String username, Long productId) {

        User user = userRepo.findByEmail(username)
                .orElseThrow(() -> new RuntimeException(Constants.USER_NOT_FOUND));

        Cart cart = cartRepo.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException(Constants.CART_NOT_FOUND));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException(Constants.CART_ITEM_IS_NULL);
        }

        cart.getItems().removeIf(item ->
                item.getProduct().getId().equals(productId)
        );

        return cartRepo.save(cart);
    }

    // =========================
    // PRIVATE HELPER METHODS
    // =========================

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
    }

    private void updateCartItemQuantity(Cart cart, Long productId, int quantity) {

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException(Constants.PRODUCT_NOT_FOUND);
        }

        boolean itemFound = false;

        for (CartItem item : cart.getItems()) {
            if (item.getProduct().getId().equals(productId)) {
                item.setQuantity(quantity);
                itemFound = true;
                break;
            }
        }

        if (!itemFound) {
            throw new RuntimeException(Constants.PRODUCT_NOT_FOUND);
        }
    }
}