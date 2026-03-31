package com.mjs.ecommerce.service;

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


    @Override
    public Cart addToCart(String username, Long productId, int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        User user = userRepo.findByName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Cart cart = cartRepo.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setItems(new ArrayList<>());
                    return cartRepo.save(newCart);
                });

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        int newQuantity = quantity;

        if (existingItem.isPresent()) {
            newQuantity = existingItem.get().getQuantity() + quantity;
        }

        if (product.getStockQuantity() < newQuantity) {
            throw new RuntimeException(
                    "Requested quantity exceeds available stock. Available: " + product.getStockQuantity()
            );
        }

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(newQuantity);
        } else {
            CartItem cartItem = new CartItem(
                    cart,
                    product,
                    quantity,
                    product.getPrice()
            );
            cart.getItems().add(cartItem);
        }

        return cartRepo.save(cart);
    }
    @Override
    public Cart getCart(Long userId) {

        return cartRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
    }

    @Override
    public Cart removeItem(Long userId, Long productId) {

        Cart cart = cartRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        cart.getItems().removeIf(item ->
                item.getProduct().getId().equals(productId)
        );

        return cartRepo.save(cart);
    }

    @Override
    public Cart updateQuantity(Long userId, Long productId, int quantity) {

        Cart cart = cartRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cart.getItems().forEach(item -> {
            if (item.getProduct().getId().equals(productId)) {
                item.setQuantity(quantity);
            }
        });

        return cartRepo.save(cart);
    }

    @Override
    public void removeall() {
        cartRepo.deleteAll();
    }
}