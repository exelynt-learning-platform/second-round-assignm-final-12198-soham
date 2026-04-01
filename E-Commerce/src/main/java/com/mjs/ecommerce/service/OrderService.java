package com.mjs.ecommerce.service;

import com.mjs.ecommerce.model.Order;

import java.util.List;

public interface OrderService {

    Order createOrder(Long userId);

    List<Order> getOrdersByUser(Long userId);

    Order getOrderById(Long orderId);
     Order createOrderByUsername(String username);
}