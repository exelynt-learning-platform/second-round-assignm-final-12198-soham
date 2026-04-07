package com.mjs.ecommerce.service;

import com.mjs.ecommerce.exception.*;
import com.mjs.ecommerce.model.*;
import com.mjs.ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    @Autowired private CartRepo cartRepo;
    @Autowired private ProductRepository productRepo;
    @Autowired private UserRepository userRepo;

    @Override
    public Cart addToCart(String username, Long productId, int quantity) {
        validateQuantity(quantity);
        User user = getUser(username);
        Product product = getProduct(productId);
        Cart cart = getOrCreateCart(user);


        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            int newTotal = existingItem.get().getQuantity() + quantity;
            validateProductStock(product, newTotal);
            existingItem.get().setQuantity(newTotal);
        } else {
            validateProductStock(product, quantity);

            CartItem newItem = new CartItem(cart, product, quantity, product.getPrice());
            cart.getItems().add(newItem);
        }
        return cartRepo.save(cart);
    }

    @Override
    public Cart getCart(Long userId) {
        return cartRepo.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user ID: " + userId));
    }

    @Override
    public Cart getCartByUsername(String username) {
        User user = getUser(username);
        return getCart(user.getId());
    }

    @Override
    public Cart updateQuantity(Long userId, Long productId, int quantity) {
        validateQuantity(quantity);
        Cart cart = getCart(userId);
        return performQuantityUpdate(cart, productId, quantity);
    }

    @Override
    public Cart updateQuantityByUsername(String username, Long productId, int quantity) {
        validateQuantity(quantity);
        User user = getUser(username);
        Cart cart = getCart(user.getId());
        return performQuantityUpdate(cart, productId, quantity);
    }

    @Override
    public Cart removeItemByUsername(String username, Long productId) {
        User user = getUser(username);
        Cart cart = getCart(user.getId());

        boolean removed = cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        if (!removed) {
            throw new ProductNotFoundException("Product not found in cart");
        }
        return cartRepo.save(cart);
    }

    // --- Private Internal Logic to avoid duplication ---

    private Cart performQuantityUpdate(Cart cart, Long productId, int quantity) {
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException("Item not in cart"));

        validateProductStock(item.getProduct(), quantity);
        item.setQuantity(quantity);
        return cartRepo.save(cart);
    }

    private Cart getOrCreateCart(User user) {
        return cartRepo.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepo.save(newCart);
                });
    }

    private User getUser(String username) {
        return userRepo.findByEmail(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }

    private Product getProduct(Long productId) {
        return productRepo.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productId));
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) throw new InvalidQuantityException("Quantity must be > 0");
    }

    private void validateProductStock(Product product, int requested) {
        if (product.getStockQuantity() < requested) {
            throw new OutOfStockException("Insufficient stock. Available: " + product.getStockQuantity());
        }
    }
}