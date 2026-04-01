package com.mjs.ecommerce.service;

import com.mjs.ecommerce.Exception.OutOfStockException;
import com.mjs.ecommerce.model.*;
import com.mjs.ecommerce.repository.CartRepo;
import com.mjs.ecommerce.repository.OrderRepo;
import com.mjs.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mjs.ecommerce.repository.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class OrderServiceImplTest {

    @InjectMocks
    private OrderServiceImpl orderServiceImpl;

    @Mock
    private OrderRepo orp;

    @Mock
    private CartRepo crp;

    @Mock
    private UserRepository userRepo;

    @Mock
    private ProductRepository repository;

    // -------------------- CREATE ORDER --------------------

    @Test
    void testCreateOrder_Success() {

        // User
        User user = new User();
        user.setId(1L);
        user.setEmail("test@mail.com");

        // Product
        Product product = new Product();
        product.setId(1L);
        product.setName("Phone");
        product.setPrice(10000);
        product.setStockQuantity(10);

        // CartItem
        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        // Cart
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setItems(new ArrayList<>(List.of(cartItem)));

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(crp.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(repository.findById(1L)).thenReturn(Optional.of(product));
        when(repository.save(any())).thenReturn(product);
        when(orp.save(any())).thenAnswer(i -> i.getArgument(0));

        Order result = orderServiceImpl.createOrder(1L);

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(20000, result.getTotalPrice());
        assertEquals(8, product.getStockQuantity());
    }

    // -------------------- EMPTY CART --------------------

    @Test
    void testCreateOrder_EmptyCart() {

        User user = new User();
        user.setId(1L);

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(crp.findByUserId(1L)).thenReturn(Optional.of(cart));

        assertThrows(IllegalArgumentException.class, () ->
                orderServiceImpl.createOrder(1L));
    }

    // -------------------- USER NOT FOUND --------------------

    @Test
    void testCreateOrder_UserNotFound() {

        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                orderServiceImpl.createOrder(1L));
    }

    // -------------------- CART NOT FOUND --------------------

    @Test
    void testCreateOrder_CartNotFound() {

        User user = new User();
        user.setId(1L);

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(crp.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                orderServiceImpl.createOrder(1L));
    }

    // -------------------- PRODUCT NOT FOUND --------------------

    @Test
    void testCreateOrder_ProductNotFound() {

        User user = new User();
        user.setId(1L);

        Product product = new Product();
        product.setId(1L);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>(List.of(cartItem)));

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(crp.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                orderServiceImpl.createOrder(1L));
    }

    // -------------------- OUT OF STOCK --------------------

    @Test
    void testCreateOrder_OutOfStock() {

        User user = new User();
        user.setId(1L);

        Product product = new Product();
        product.setId(1L);
        product.setName("Phone");
        product.setStockQuantity(1);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(5);

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>(List.of(cartItem)));

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(crp.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(repository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(OutOfStockException.class, () ->
                orderServiceImpl.createOrder(1L));
    }

    // -------------------- GET ORDERS BY USER --------------------

    @Test
    void testGetOrdersByUser() {

        List<Order> orders = List.of(new Order(), new Order());

        when(orp.findByUserId(1L)).thenReturn(orders);

        List<Order> result = orderServiceImpl.getOrdersByUser(1L);

        assertEquals(2, result.size());
    }

    // -------------------- GET ORDER BY ID --------------------

    @Test
    void testGetOrderById_Success() {

        Order order = new Order();

        when(orp.findById(1L)).thenReturn(Optional.of(order));

        Order result = orderServiceImpl.getOrderById(1L);

        assertNotNull(result);
    }

    @Test
    void testGetOrderById_NotFound() {

        when(orp.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                orderServiceImpl.getOrderById(1L));
    }

    // -------------------- CREATE ORDER BY USERNAME --------------------


}