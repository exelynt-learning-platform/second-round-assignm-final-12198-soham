package com.mjs.ecommerce.controller;

import com.mjs.ecommerce.model.Order;
import com.mjs.ecommerce.service.OrderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderServiceImpl orderService;

    // CREATE ORDER (Cart → Order)
    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Order> createOrder(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {

        String username = userDetails.getUsername();

        Order order = orderService.createOrderByUsername(username);

        return ResponseEntity.status(201).body(order);
    }

    // GET ALL ORDERS OF USER
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Order>> getOrders(@PathVariable Long userId) {

        return ResponseEntity.ok(orderService.getOrdersByUser(userId));
    }

    // GET ORDER BY ID
    @GetMapping("/details/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }
}