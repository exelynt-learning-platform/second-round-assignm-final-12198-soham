package com.mjs.ecommerce.service;

import com.mjs.ecommerce.Constants;
import com.mjs.ecommerce.model.OrderItem;
import com.mjs.ecommerce.repository.OrderItemRepo;
import org.junit.jupiter.api.Test;


import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceImplTest {

    @Mock
    private OrderItemRepo oir;

    @InjectMocks
    private OrderItemServiceImpl orderItemServiceImpl;

    @Test
    void createOrderItem_Success() {
        OrderItem item = new OrderItem();
        item.setQuantity(2);

        when(oir.save(item)).thenReturn(item);

        OrderItem result = orderItemServiceImpl.createOrderItem(item);

        assertNotNull(result);
        assertEquals(2, result.getQuantity());
        verify(oir, times(1)).save(item);
    }

    @Test
    void getAllOrderItems_Success() {
        List<OrderItem> items = List.of(new OrderItem(), new OrderItem());

        when(oir.findAll()).thenReturn(items);

        List<OrderItem> result = orderItemServiceImpl.getAllOrderItems();

        assertEquals(2, result.size());
    }

    @Test
    void getOrderItemById_Success() {
        OrderItem item = new OrderItem();
        item.setId(1L);

        when(oir.findById(1L)).thenReturn(Optional.of(item));

        OrderItem result = orderItemServiceImpl.getOrderItemById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getOrderItemById_NotFound() {
        when(oir.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                orderItemServiceImpl.getOrderItemById(1L)
        );

        assertTrue(ex.getMessage().contains(Constants.ORDER_NOT_FOUND));
    }

    @Test
    void deleteOrderItem_Success() {
        OrderItem item = new OrderItem();
        item.setId(1L);

        when(oir.findById(1L)).thenReturn(Optional.of(item));

        orderItemServiceImpl.deleteOrderItem(1L);

        verify(oir, times(1)).delete(item);
    }

    @Test
    void deleteOrderItem_NotFound() {
        when(oir.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                orderItemServiceImpl.deleteOrderItem(1L)
        );

        assertTrue(ex.getMessage().contains(Constants.ORDER_NOT_FOUND));
    }
}