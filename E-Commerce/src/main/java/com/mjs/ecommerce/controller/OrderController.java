package com.mjs.ecommerce.controller;

import com.mjs.ecommerce.model.Order;
import com.mjs.ecommerce.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService service;

    // CREATE ORDER (Cart → Order)
    @PostMapping("/create/{userId}")
    public ResponseEntity<Order> createOrder(@PathVariable Long userId) {

        Order order = service.createOrder(userId);
        return ResponseEntity.status(201).body(order);
    }

    // GET ALL ORDERS OF USER
    @GetMapping("/{userId}")
    public ResponseEntity<List<Order>> getOrders(@PathVariable Long userId) {

        return ResponseEntity.ok(service.getOrdersByUser(userId));
    }

    // GET ORDER BY ID
    @GetMapping("/details/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId) {

        return ResponseEntity.ok(service.getOrderById(orderId));
    }
}