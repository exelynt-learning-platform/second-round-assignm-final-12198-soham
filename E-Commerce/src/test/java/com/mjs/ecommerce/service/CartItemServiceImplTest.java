package com.mjs.ecommerce.service;

import com.mjs.ecommerce.model.CartItem;
import com.mjs.ecommerce.repository.CartItemRepository;
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
class CartItemServiceImplTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private CartItemServiceImpl cartItemService;

    private CartItem cartItem;

    @BeforeEach
    void setup() {
        cartItem = new CartItem();
        cartItem.setPrice(21);
        cartItem.setQuantity(2);
    }

    // ✅ Test createCartItem
    @Test
    void testCreateCartItem() {
        when(cartItemRepository.save(cartItem)).thenReturn(cartItem);

        CartItem savedItem = cartItemService.createCartItem(cartItem);

        assertNotNull(savedItem);
        assertEquals(null, savedItem.getId());
        verify(cartItemRepository, times(1)).save(cartItem);
    }


    @Test
    void testGetAllCartItems() {
        List<CartItem> list = Arrays.asList(cartItem);

        when(cartItemRepository.findAll()).thenReturn(list);

        List<CartItem> result = cartItemService.getAllCartItems();

        assertEquals(1, result.size());
        verify(cartItemRepository, times(1)).findAll();
    }


    @Test
    void testGetCartItemById_Success() {
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));

        CartItem result = cartItemService.getCartItemById(1L);

        assertNotNull(result);
        assertEquals(null, result.getId());
        verify(cartItemRepository, times(1)).findById(1L);
    }


    @Test
    void testGetCartItemById_NotFound() {
        when(cartItemRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                cartItemService.getCartItemById(1L));

        assertEquals("CartItem not found", exception.getMessage());
    }


    @Test
    void testDeleteCartItem_Success() {
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));

        cartItemService.deleteCartItem(1L);

        verify(cartItemRepository, times(1)).delete(cartItem);
    }


    @Test
    void testDeleteCartItem_NotFound() {
        when(cartItemRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                cartItemService.deleteCartItem(1L));

        assertEquals("CartItem not found", exception.getMessage());
        verify(cartItemRepository, never()).delete(any());
    }
}