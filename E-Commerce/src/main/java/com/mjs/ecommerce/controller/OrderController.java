package com.mjs.ecommerce.controller;

import com.mjs.ecommerce.model.Order;
import com.mjs.ecommerce.service.OrderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderServiceImpl orderService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Order> createOrder(
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        Order order = orderService.createOrderByUsername(username);
        return ResponseEntity.status(201).body(order);
    }

    // ✅ FIXED: Now uses authenticated user's username instead of userId path variable
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Order>> getOrders(
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        return ResponseEntity.ok(orderService.getOrdersByUsername(username));
    }

    // ✅ FIXED: Verify order belongs to authenticated user before returning
    @GetMapping("/details/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Order> getOrderById(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        Order order = orderService.getOrderById(orderId);

        if (!order.getUser().getEmail().equals(username)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(order);
    }
}