package com.mjs.ecommerce.service;

import com.mjs.ecommerce.model.*;
import com.mjs.ecommerce.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class CartServiceImplTest {

    @InjectMocks
    private CartServiceImpl cartService;

    @Mock
    private CartRepo cartRepo;

    @Mock
    private ProductRepository productRepo;

    @Mock
    private UserRepository userRepo;

    // -------------------- ADD TO CART --------------------



    @Test
    void testAddToCart_ProductNotFound() {

        when(userRepo.findByEmail(any())).thenReturn(Optional.of(new User()));
        when(productRepo.findById(any())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                cartService.addToCart("test@mail.com", 1L, 2));
    }

    @Test
    void testAddToCart_StockExceeded() {

        User user = new User();
        user.setId(1L);
        user.setName("soham");
        user.setEmail("soham@test.com");
        Product product = new Product();
        product.setId(1L);
        product.setName("p1");
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setItems(new ArrayList<>());

        when(userRepo.findByEmail(any())).thenReturn(Optional.of(user));
        when(productRepo.findById(any())).thenReturn(Optional.of(product));
        when(cartRepo.findByUserId(any())).thenReturn(Optional.of(cart));

        assertThrows(RuntimeException.class, () ->
                cartService.addToCart("test@mail.com", 1L, 5));
    }

    // -------------------- GET CART --------------------

    @Test
    void testGetCart_Success() {

        Cart cart = new Cart();

        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(cart));

        Cart result = cartService.getCart(1L);

        assertNotNull(result);
    }

    @Test
    void testGetCart_NotFound() {

        when(cartRepo.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                cartService.getCart(1L));
    }

    // -------------------- UPDATE QUANTITY --------------------

    @Test
    void testUpdateQuantity_Success() {

        User user = new User();
        user.setId(1L);
        user.setName("soham");
        user.setEmail("soham@test.com");
        Product product = new Product();
        product.setId(1L);
        product.setName("p1");
        CartItem item = new CartItem(null, product, 2, 10000);

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>(List.of(item)));

        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepo.save(any())).thenReturn(cart);

        Cart result = cartService.updateQuantity(1L, 1L, 5);

        assertEquals(5, result.getItems().get(0).getQuantity());
    }

    @Test
    void testUpdateQuantity_InvalidQuantity() {

        assertThrows(IllegalArgumentException.class, () ->
                cartService.updateQuantity(1L, 1L, 0));
    }

    @Test
    void testUpdateQuantity_ProductNotFound() {

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());

        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(cart));

        assertThrows(RuntimeException.class, () ->
                cartService.updateQuantity(1L, 1L, 5));
    }

    // -------------------- REMOVE ITEM --------------------

    @Test
    void testRemoveItemByUsername_Success() {

        User user = new User();
        user.setId(1L);
        user.setName("soham");
        user.setEmail("soham@test.com");
        Product product = new Product();
        product.setId(1L);
        product.setName("p1");
        CartItem item = new CartItem(null, product, 2, 10000);

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>(List.of(item)));

        when(userRepo.findByEmail(any())).thenReturn(Optional.of(user));
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepo.save(any())).thenReturn(cart);

        Cart result = cartService.removeItemByUsername("test@mail.com", 1L);

        assertEquals(0, result.getItems().size());
    }
}