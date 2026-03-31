package com.mjs.ecommerce.service;

import com.mjs.ecommerce.enums.OrderStatus;
import com.mjs.ecommerce.enums.PaymentStatus;
import com.mjs.ecommerce.model.*;
import com.mjs.ecommerce.repository.CartRepo;
import com.mjs.ecommerce.repository.OrderRepo;
import com.mjs.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import org.mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepo orp;

    @Mock
    private CartRepo crp;

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrder_Success() {
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setName("John");

        Product product = new Product();
        product.setId(10L);
        product.setPrice(100.0);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setItems(new ArrayList<>(List.of(cartItem)));

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(crp.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(crp.save(any())).thenReturn(cart);
        when(orp.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.createOrder(userId);

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(200.0, result.getTotalPrice());
        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
        assertEquals(PaymentStatus.PENDING, result.getPaymentStatus());

        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void createOrder_UserNotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                orderService.createOrder(1L)
        );
    }

    @Test
    void createOrder_CartNotFound() {
        User user = new User();
        user.setId(1L);

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(crp.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                orderService.createOrder(1L)
        );
    }

    @Test
    void createOrder_EmptyCart() {
        User user = new User();
        user.setId(1L);
        user.setName("John");

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setItems(new ArrayList<>());

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(crp.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(crp.save(any())).thenReturn(cart);
        when(orp.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.createOrder(1L);

        assertNotNull(result);
        assertEquals(0, result.getItems().size());
        assertEquals(0.0, result.getTotalPrice());
    }

    @Test
    void getOrdersByUser_Success() {
        List<Order> orders = List.of(new Order(), new Order());

        when(orp.findByUserId(1L)).thenReturn(orders);

        List<Order> result = orderService.getOrdersByUser(1L);

        assertEquals(2, result.size());
    }

    @Test
    void getOrderById_Success() {
        Order order = new Order();

        when(orp.findById(1L)).thenReturn(Optional.of(order));

        Order result = orderService.getOrderById(1L);

        assertNotNull(result);
    }

    @Test
    void getOrderById_NotFound() {
        when(orp.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                orderService.getOrderById(1L)
        );
    }
}