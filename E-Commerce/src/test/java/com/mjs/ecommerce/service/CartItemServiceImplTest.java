package com.mjs.ecommerce.service;
import com.mjs.ecommerce.model.CartItem;
import com.mjs.ecommerce.repository.CartItemRepository;
import org.junit.jupiter.api.Test;
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
    private CartItemRepository cir;

    @InjectMocks
    private CartItemServiceImpl cartItemServiceImpl;

    @Test
    void createCartItem_Success() {
        CartItem item = new CartItem();
        item.setQuantity(2);

        when(cir.save(item)).thenReturn(item);

        CartItem result = cartItemServiceImpl.createCartItem(item);

        assertNotNull(result);
        assertEquals(2, result.getQuantity());
        verify(cir, times(1)).save(item);
    }

    @Test
    void getAllCartItems_Success() {
        List<CartItem> items = List.of(new CartItem(), new CartItem());

        when(cir.findAll()).thenReturn(items);

        List<CartItem> result = cartItemServiceImpl.getAllCartItems();

        assertEquals(2, result.size());
    }

    @Test
    void getCartItemById_Success() {
        CartItem item = new CartItem();
        item.setId(1L);

        when(cir.findById(1L)).thenReturn(Optional.of(item));

        CartItem result = cartItemServiceImpl.getCartItemById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getCartItemById_NotFound() {
        when(cir.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                cartItemServiceImpl.getCartItemById(1L)
        );
    }

    @Test
    void deleteCartItem_Success() {
        CartItem item = new CartItem();
        item.setId(1L);

        when(cir.findById(1L)).thenReturn(Optional.of(item));

        cartItemServiceImpl.deleteCartItem(1L);

        verify(cir, times(1)).delete(item);
    }

    @Test
    void deleteCartItem_NotFound() {
        when(cir.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                cartItemServiceImpl.deleteCartItem(1L)
        );
    }
}