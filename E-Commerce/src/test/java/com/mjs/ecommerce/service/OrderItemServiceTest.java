package com.mjs.ecommerce.service;

import com.mjs.ecommerce.model.OrderItem;
import com.mjs.ecommerce.repository.OrderItemRepo;
import org.junit.jupiter.api.Test;



import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceTest {

    @Mock
    private OrderItemRepo orderItemRepo;

    @InjectMocks
    private OrderItemService orderItemService;

    private OrderItem orderItem;

    @BeforeEach
    void setup() {
        orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setQuantity(3);
    }

    // ✅ createOrderItem
    @Test
    void testCreateOrderItem() {
        when(orderItemRepo.save(orderItem)).thenReturn(orderItem);

        OrderItem saved = orderItemService.createOrderItem(orderItem);

        assertNotNull(saved);
        assertEquals(1L, saved.getId());
        verify(orderItemRepo, times(1)).save(orderItem);
    }

    // ✅ getAllOrderItems
    @Test
    void testGetAllOrderItems() {
        List<OrderItem> list = Arrays.asList(orderItem);

        when(orderItemRepo.findAll()).thenReturn(list);

        List<OrderItem> result = orderItemService.getAllOrderItems();

        assertEquals(1, result.size());
        verify(orderItemRepo, times(1)).findAll();
    }

    // ✅ getOrderItemById - success
    @Test
    void testGetOrderItemById_Success() {
        when(orderItemRepo.findById(1L)).thenReturn(Optional.of(orderItem));

        OrderItem result = orderItemService.getOrderItemById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(orderItemRepo, times(1)).findById(1L);
    }

    // ❌ getOrderItemById - not found
    @Test
    void testGetOrderItemById_NotFound() {
        when(orderItemRepo.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderItemService.getOrderItemById(1L));

        assertEquals("OrderItem not found", ex.getMessage());
    }

    // ✅ deleteOrderItem - success
    @Test
    void testDeleteOrderItem_Success() {
        when(orderItemRepo.findById(1L)).thenReturn(Optional.of(orderItem));

        orderItemService.deleteOrderItem(1L);

        verify(orderItemRepo, times(1)).delete(orderItem);
    }


    @Test
    void testDeleteOrderItem_NotFound() {
        when(orderItemRepo.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderItemService.deleteOrderItem(1L));

        assertEquals("OrderItem not found", ex.getMessage());
        verify(orderItemRepo, never()).delete(any());
    }
}