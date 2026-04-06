package com.mjs.ecommerce.service;

import com.mjs.ecommerce.exception.CartItemNotFoundException;
import com.mjs.ecommerce.exception.CartNotFoundException;
import com.mjs.ecommerce.exception.InvalidQuantityException;
import com.mjs.ecommerce.exception.OutOfStockException;
import com.mjs.ecommerce.exception.ProductNotFoundException;
import com.mjs.ecommerce.exception.UserNotFoundException;
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

        User user = getUser(username);
        Product product = getProduct(productId);
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

    private Product getProduct(Long productId) {
        return productRepo.findById(productId).orElseThrow(ProductNotFoundException::new);
    }

    private User getUser(String username) {
        return userRepo.findByEmail(username)
                .orElseThrow(UserNotFoundException::new);

    }

    @Override
    public Cart getCart(Long userId) {
        return cartRepo.findByUserId(userId)
                .orElseThrow(CartNotFoundException::new);
    }

    @Override
    public Cart getCartByUsername(String username) {
        User user = getUser(username);

        return cartRepo.findByUserId(user.getId())
                .orElseThrow(CartNotFoundException::new);
    }

    @Override
    public Cart updateQuantity(Long userId, Long productId, int quantity) {
        return updateQuantityInternal(userId, productId, quantity);
    }

    @Override
    public Cart updateQuantityByUsername(String username, Long productId, int quantity) {
        User user = getUser(username);

        return updateQuantityInternal(user.getId(), productId, quantity);
    }

    @Override
    public Cart removeItemByUsername(String username, Long productId) {
        User user = getUser(username);

        Cart cart = cartRepo.findByUserId(user.getId())
                .orElseThrow(CartNotFoundException::new);

        List<CartItem> items = getItemsOrEmpty(cart);

        if (items.isEmpty()) {
            throw new CartItemNotFoundException();
        }

        boolean itemExists = items.stream()
                .anyMatch(item -> item.getProduct().getId().equals(productId));

        if (!itemExists) {
            throw new ProductNotFoundException("Product with ID " + productId + " not found in cart");
        }

        getMutableItems(cart).removeIf(item -> item.getProduct().getId().equals(productId));

        return cartRepo.save(cart);
    }

    public Cart clearCart(String username) {
        User user = getUser(username);

        Cart cart = cartRepo.findByUserId(user.getId())
                .orElseThrow(CartNotFoundException::new);

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
                .orElseThrow(CartNotFoundException::new);

        updateCartItemQuantityWithValidation(cart, productId, quantity);

        return cartRepo.save(cart);
    }

    private void updateCartItemQuantityWithValidation(Cart cart, Long productId, int newQuantity) {
        List<CartItem> items = getItemsOrEmpty(cart);

        if (items.isEmpty()) {
            throw new ProductNotFoundException();
        }

        CartItem itemToUpdate = items.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(ProductNotFoundException::new);

        validateProductStock(itemToUpdate.getProduct(), newQuantity);
        itemToUpdate.setQuantity(newQuantity);
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new InvalidQuantityException();
        }
    }

    private void validateProductStock(Product product, int requestedQuantity) {
        if (product.getStockQuantity() < requestedQuantity) {
            throw new OutOfStockException(
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