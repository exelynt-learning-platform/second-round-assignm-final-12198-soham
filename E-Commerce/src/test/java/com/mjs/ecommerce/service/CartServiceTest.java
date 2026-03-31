package com.mjs.ecommerce.service;

import com.mjs.ecommerce.model.*;
import com.mjs.ecommerce.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepo cartRepo;

    @Mock
    private ProductRepository productRepo;

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Product product;
    private Cart cart;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        product = new Product();
        product.setId(1L);
        product.setPrice(10.0);

        cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
        cart.setItems(new ArrayList<>());
    }

    @Test
    void addToCart_UserNotFound_ThrowsException() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cartService.addToCart(1L, 1L, 1);
        });

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void addToCart_ProductNotFound_ThrowsException() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(productRepo.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cartService.addToCart(1L, 1L, 1);
        });

        assertEquals("Product not found", exception.getMessage());
    }

    @Test
    void addToCart_CartExists_ProductNotInCart_AddsNewItem() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(productRepo.findById(1L)).thenReturn(Optional.of(product));
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepo.save(cart)).thenReturn(cart);

        Cart result = cartService.addToCart(1L, 1L, 2);

        assertEquals(cart, result);
        assertEquals(1, cart.getItems().size());
        CartItem item = cart.getItems().get(0);
        assertEquals(product, item.getProduct());
        assertEquals(2, item.getQuantity());
        assertEquals(10.0, item.getPrice());
        assertEquals(10.0, item.getPriceAtAddition());
        verify(cartRepo, times(1)).save(cart);
    }

    @Test
    void addToCart_CartExists_ProductInCart_UpdatesQuantity() {
        CartItem existingItem = new CartItem();
        existingItem.setProduct(product);
        existingItem.setQuantity(1);
        cart.getItems().add(existingItem);

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(productRepo.findById(1L)).thenReturn(Optional.of(product));
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepo.save(cart)).thenReturn(cart);

        Cart result = cartService.addToCart(1L, 1L, 3);

        assertEquals(cart, result);
        assertEquals(1, cart.getItems().size());
        assertEquals(4, existingItem.getQuantity());
        verify(cartRepo, times(1)).save(cart);
    }

    @Test
    void addToCart_CartDoesNotExist_CreatesCartAndAddsItem() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(productRepo.findById(1L)).thenReturn(Optional.of(product));
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.empty());
        when(cartRepo.save(any(Cart.class))).thenReturn(cart);

        Cart result = cartService.addToCart(1L, 1L, 1);

        assertEquals(cart, result);
        verify(cartRepo, times(2)).save(any(Cart.class)); // once for new cart, once for update
    }
}
