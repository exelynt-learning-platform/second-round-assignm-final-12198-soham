package com.mjs.ecommerce.service;

import com.mjs.ecommerce.model.*;
import com.mjs.ecommerce.repository.CartRepo;
import com.mjs.ecommerce.repository.ProductRepository;
import com.mjs.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class CartServiceImplImplTest {

    @InjectMocks
    private CartServiceImpl service;

    @Mock
    private CartRepo cartRepo;

    @Mock
    private ProductRepository productRepo;

    @Mock
    private UserRepository userRepo;

    // -------------------------
    // Helper methods
    // -------------------------
    private User user() {
        User u = new User();
        u.setId(1L);
        u.setEmail("test@test.com");
        return u;
    }

    private Product product() {
        Product p = new Product();
        p.setId(1L);
        p.setPrice(100.0);
        p.setStockQuantity(10);
        return p;
    }

    private Cart cart(User user) {
        Cart c = new Cart();
        c.setUser(user);
        c.setItems(new ArrayList<>());
        return c;
    }

    // =========================
    // ADD TO CART
    // =========================

    @Test
    void addToCart_newCart_success() {

        User user = user();
        Product product = product();

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(productRepo.findById(1L)).thenReturn(Optional.of(product));
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.empty());
        when(cartRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        Cart result = service.addToCart("test@test.com", 1L, 2);

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
    }


    @Test
    void addToCart_invalidQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> service.addToCart("test@test.com", 1L, 0));
    }

    @Test
    void addToCart_stockExceeded() {

        User user = user();
        Product product = product();
        product.setStockQuantity(2);

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(productRepo.findById(1L)).thenReturn(Optional.of(product));
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(cart(user)));

        assertThrows(RuntimeException.class,
                () -> service.addToCart("test@test.com", 1L, 5));
    }

    // =========================
    // GET CART
    // =========================

    @Test
    void getCart_success() {

        Cart cart = new Cart();

        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(cart));

        assertNotNull(service.getCart(1L));
    }

    @Test
    void getCart_notFound() {

        when(cartRepo.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.getCart(1L));
    }

    // =========================
    // UPDATE QUANTITY
    // =========================


    @Test
    void updateQuantity_invalidQuantity() {

        assertThrows(IllegalArgumentException.class,
                () -> service.updateQuantity(1L, 1L, 0));
    }

    @Test
    void updateQuantity_productNotFound() {

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());

        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(cart));

        assertThrows(RuntimeException.class,
                () -> service.updateQuantity(1L, 1L, 2));
    }

    // =========================
    // REMOVE ITEM
    // =========================


    // =========================
    // UPDATE BY USERNAME
    // =========================


    @Test
    void updateQuantityByUsername_productNotFound() {

        User user = user();

        Cart cart = cart(user);

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(cart));

        assertThrows(RuntimeException.class,
                () -> service.updateQuantityByUsername("test@test.com", 1L, 5));
    }
}