package com.mjs.ecommerce.service;

import com.mjs.ecommerce.model.*;
import com.mjs.ecommerce.repository.CartRepo;
import com.mjs.ecommerce.repository.OrderRepo;
import com.mjs.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.Test;




import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepo orderRepo;

    @Mock
    private CartRepo cartRepo;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Cart cart;
    private CartItem cartItem;
    private Product product;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);

        product = new Product();
        product.setId(101L);
        product.setPrice(100.0);

        cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        cart = new Cart();
        cart.setId(1L);
        cart.setUser(user);
        cart.setItems(new ArrayList<>(List.of(cartItem)));
    }

    // ✅ createOrder - success
    @Test
    void testCreateOrder_Success() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(orderRepo.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order order = orderService.createOrder(1L);

        assertNotNull(order);
        assertEquals("CREATED", order.getStatus());
        assertEquals("PENDING", order.getPaymentStatus());
        assertEquals(200.0, order.getTotalPrice());

        // Order items validation
        assertEquals(1, order.getItems().size());
        OrderItem oi = order.getItems().get(0);
        assertEquals(product, oi.getProduct());
        assertEquals(2, oi.getQuantity());

        // Cart should be cleared
        assertTrue(cart.getItems().isEmpty());

        verify(cartRepo, times(1)).save(cart);
        verify(orderRepo, times(1)).save(order);
    }

    // ❌ createOrder - user not found
    @Test
    void testCreateOrder_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(1L));

        assertEquals("User not found", ex.getMessage());
    }

    // ❌ createOrder - cart not found
    @Test
    void testCreateOrder_CartNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(1L));

        assertEquals("Cart not found", ex.getMessage());
    }

    // ✅ getOrdersByUser
    @Test
    void testGetOrdersByUser() {
        List<Order> orders = List.of(new Order());

        when(orderRepo.findByUserId(1L)).thenReturn(orders);

        List<Order> result = orderService.getOrdersByUser(1L);

        assertEquals(1, result.size());
        verify(orderRepo, times(1)).findByUserId(1L);
    }

    // ✅ getOrderById - success
    @Test
    void testGetOrderById_Success() {
        Order order = new Order();
        order.setId(1L);

        when(orderRepo.findById(1L)).thenReturn(Optional.of(order));

        Order result = orderService.getOrderById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    // ❌ getOrderById - not found
    @Test
    void testGetOrderById_NotFound() {
        when(orderRepo.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.getOrderById(1L));

        assertEquals("Order not found", ex.getMessage());
    }
}