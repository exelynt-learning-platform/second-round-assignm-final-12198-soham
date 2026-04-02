package com.mjs.ecommerce.service;

import com.mjs.ecommerce.constants.Constants;
import com.mjs.ecommerce.model.Cart;
import com.mjs.ecommerce.model.CartItem;
import com.mjs.ecommerce.model.Product;
import com.mjs.ecommerce.model.User;
import com.mjs.ecommerce.repository.CartRepo;
import com.mjs.ecommerce.repository.ProductRepository;
import com.mjs.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Override
    public Cart addToCart(String username, Long productId, int quantity) {
        validateQuantity(quantity);

        User user = userRepo.findByEmail(username)
                .orElseThrow(() -> new RuntimeException(Constants.USER_NOT_FOUND));

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException(Constants.PRODUCT_NOT_FOUND));

        Cart cart = getOrCreateCart(user);
        List<CartItem> items = getMutableItems(cart);

        Optional<CartItem> existingItem = items.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        int newQuantity = existingItem
                .map(item -> item.getQuantity() + quantity)
                .orElse(quantity);

        validateProductStock(product, newQuantity);

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(newQuantity);
        } else {
            items.add(new CartItem(cart, product, quantity, product.getPrice()));
        }

        return cartRepo.save(cart);
    }

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

    @Override
    public Cart updateQuantity(Long userId, Long productId, int quantity) {
        return updateQuantityInternal(userId, productId, quantity);
    }

    @Override
    public Cart updateQuantityByUsername(String username, Long productId, int quantity) {
        User user = userRepo.findByEmail(username)
                .orElseThrow(() -> new RuntimeException(Constants.USER_NOT_FOUND));

        return updateQuantityInternal(user.getId(), productId, quantity);
    }

    @Override
    public Cart removeItemByUsername(String username, Long productId) {
        User user = userRepo.findByEmail(username)
                .orElseThrow(() -> new RuntimeException(Constants.USER_NOT_FOUND));

        Cart cart = cartRepo.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException(Constants.CART_NOT_FOUND));

        List<CartItem> items = getItemsOrEmpty(cart);

        if (items.isEmpty()) {
            throw new RuntimeException(Constants.CART_ITEM_IS_NULL);
        }

        boolean itemExists = items.stream()
                .anyMatch(item -> item.getProduct().getId().equals(productId));

        if (!itemExists) {
            throw new RuntimeException("Product with ID " + productId + " not found in cart");
        }

        getMutableItems(cart).removeIf(item -> item.getProduct().getId().equals(productId));

        return cartRepo.save(cart);
    }

    public Cart clearCart(String username) {
        User user = userRepo.findByEmail(username)
                .orElseThrow(() -> new RuntimeException(Constants.USER_NOT_FOUND));

        Cart cart = cartRepo.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException(Constants.CART_NOT_FOUND));

        getMutableItems(cart).clear();

        return cartRepo.save(cart);
    }

    public double getCartTotal(String username) {
        Cart cart = getCartByUsername(username);

        return getItemsOrEmpty(cart).stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    public int getCartItemCount(String username) {
        Cart cart = getCartByUsername(username);

        return getItemsOrEmpty(cart).stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    private Cart updateQuantityInternal(Long userId, Long productId, int quantity) {
        validateQuantity(quantity);

        Cart cart = cartRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException(Constants.CART_NOT_FOUND));

        updateCartItemQuantityWithValidation(cart, productId, quantity);

        return cartRepo.save(cart);
    }

    private void updateCartItemQuantityWithValidation(Cart cart, Long productId, int newQuantity) {
        List<CartItem> items = getItemsOrEmpty(cart);

        if (items.isEmpty()) {
            throw new RuntimeException(Constants.PRODUCT_NOT_FOUND);
        }

        CartItem itemToUpdate = items.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(Constants.PRODUCT_NOT_FOUND));

        validateProductStock(itemToUpdate.getProduct(), newQuantity);
        itemToUpdate.setQuantity(newQuantity);
    }

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

    private Cart getOrCreateCart(User user) {
        return cartRepo.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setItems(new ArrayList<>());
                    return cartRepo.save(newCart);
                });
    }

    private List<CartItem> getItemsOrEmpty(Cart cart) {
        return Optional.ofNullable(cart.getItems()).orElse(Collections.emptyList());
    }

    private List<CartItem> getMutableItems(Cart cart) {
        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }
        return cart.getItems();
    }
}