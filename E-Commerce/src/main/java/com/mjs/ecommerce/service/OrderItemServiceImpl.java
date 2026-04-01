package com.mjs.ecommerce.service;

import com.mjs.ecommerce.constants.Constants;
import com.mjs.ecommerce.model.OrderItem;
import com.mjs.ecommerce.repository.OrderItemRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderItemServiceImpl implements OrderItemService {
    @Autowired
    private OrderItemRepo oir;

    @Override
    public OrderItem createOrderItem(OrderItem orderItem) {
        return oir.save(orderItem);
    }

    @Override
    public List<OrderItem> getAllOrderItems() {
        return oir.findAll();
    }

    @Override
    public OrderItem getOrderItemById(Long id) {
        return oir.findById(id)
                .orElseThrow(() -> new RuntimeException(Constants.ORDER_NOT_FOUND));
    }

    @Override
    public void deleteOrderItem(Long id) {

        OrderItem item = oir.findById(id)
                .orElseThrow(() -> new RuntimeException(Constants.ORDER_NOT_FOUND));

        oir.delete(item);
    }
}
