package com.mjs.ecommerce.service;

import com.mjs.ecommerce.model.OrderItem;

import java.util.List;

public interface OrderItemService {
    OrderItem createOrderItem(OrderItem orderItem);

    List<OrderItem> getAllOrderItems();

    OrderItem getOrderItemById(Long id);

    void deleteOrderItem(Long id);
}
