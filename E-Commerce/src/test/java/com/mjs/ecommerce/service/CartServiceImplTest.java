package com.mjs.ecommerce.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import com.mjs.ecommerce.model.Cart;
import com.mjs.ecommerce.model.CartItem;
import com.mjs.ecommerce.model.Product;
import com.mjs.ecommerce.model.User;
import com.mjs.ecommerce.repository.CartRepo;
import com.mjs.ecommerce.repository.ProductRepository;
import com.mjs.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepo cartRepo;

    @Mock
    private ProductRepository productRepo;

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private CartServiceImpl cartService;

    // ✅ ADD TO CART - NEW ITEM
    @Test
    void addToCart_NewItem_Success() {
        User user = new User();
        user.setId(1L);
        user.setName("john");

        Product product = new Product();
        product.setId(10L);
        product.setPrice(100.0);
        product.setStockQuantity(10);

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setItems(new ArrayList<>());

        when(userRepo.findByName("john")).thenReturn(Optional.of(user));
        when(productRepo.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepo.save(any(Cart.class))).thenReturn(cart);

        Cart result = cartService.addToCart("john", 10L, 2);

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(2, result.getItems().get(0).getQuantity());
    }

    // ✅ ADD TO CART - EXISTING ITEM
    @Test
    void addToCart_ExistingItem_UpdateQuantity() {
        User user = new User();
        user.setId(1L);

        Product product = new Product();
        product.setId(10L);
        product.setPrice(100.0);
        product.setStockQuantity(10);

        CartItem existingItem = new CartItem();
        existingItem.setProduct(product);
        existingItem.setQuantity(3);

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setItems(new ArrayList<>(List.of(existingItem)));

        when(userRepo.findByName("john")).thenReturn(Optional.of(user));
        when(productRepo.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepo.save(any())).thenReturn(cart);

        Cart result = cartService.addToCart("john", 10L, 2);

        assertEquals(5, result.getItems().get(0).getQuantity());
    }

    // ❌ INSUFFICIENT STOCK
    @Test
    void addToCart_InsufficientStock_ThrowsException() {
        User user = new User();
        user.setId(1L);

        Product product = new Product();
        product.setId(10L);
        product.setStockQuantity(2);

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setItems(new ArrayList<>());

        when(userRepo.findByName("john")).thenReturn(Optional.of(user));
        when(productRepo.findById(10L)).thenReturn(Optional.of(product));
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(cart));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                cartService.addToCart("john", 10L, 5)
        );

        assertTrue(ex.getMessage().contains("exceeds available stock"));
    }

    // ❌ INVALID QUANTITY
    @Test
    void addToCart_InvalidQuantity_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                cartService.addToCart("john", 10L, 0)
        );
    }

    // ❌ USER NOT FOUND
    @Test
    void addToCart_UserNotFound_ThrowsException() {
        when(userRepo.findByName("john")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                cartService.addToCart("john", 10L, 1)
        );
    }

    // ❌ PRODUCT NOT FOUND
    @Test
    void addToCart_ProductNotFound_ThrowsException() {
        User user = new User();
        user.setId(1L);

        when(userRepo.findByName("john")).thenReturn(Optional.of(user));
        when(productRepo.findById(10L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                cartService.addToCart("john", 10L, 1)
        );
    }

    // ✅ GET CART
    @Test
    void getCart_Success() {
        Cart cart = new Cart();

        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(cart));

        Cart result = cartService.getCart(1L);

        assertNotNull(result);
    }

    // ❌ GET CART NOT FOUND
    @Test
    void getCart_NotFound() {
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                cartService.getCart(1L)
        );
    }

    // ✅ REMOVE ITEM
    @Test
    void removeItem_Success() {
        Product product = new Product();
        product.setId(10L);

        CartItem item = new CartItem();
        item.setProduct(product);

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>(List.of(item)));

        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepo.save(any())).thenReturn(cart);

        Cart result = cartService.removeItem(1L, 10L);

        assertTrue(result.getItems().isEmpty());
    }

    // ❌ REMOVE ITEM - CART NOT FOUND
    @Test
    void removeItem_CartNotFound() {
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                cartService.removeItem(1L, 10L)
        );
    }

    // ✅ UPDATE QUANTITY
    @Test
    void updateQuantity_Success() {
        Product product = new Product();
        product.setId(10L);

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(1);

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>(List.of(item)));

        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepo.save(any())).thenReturn(cart);

        Cart result = cartService.updateQuantity(1L, 10L, 5);

        assertEquals(5, result.getItems().get(0).getQuantity());
    }

    // ❌ UPDATE QUANTITY - CART NOT FOUND
    @Test
    void updateQuantity_CartNotFound() {
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                cartService.updateQuantity(1L, 10L, 5)
        );
    }

    // ✅ REMOVE ALL
    @Test
    void removeAll_Success() {
        doNothing().when(cartRepo).deleteAll();

        cartService.removeall();

        verify(cartRepo, times(1)).deleteAll();
    }
}