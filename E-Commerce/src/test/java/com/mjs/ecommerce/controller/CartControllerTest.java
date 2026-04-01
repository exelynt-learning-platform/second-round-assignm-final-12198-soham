package com.mjs.ecommerce.controller;

import com.mjs.ecommerce.model.Cart;
import com.mjs.ecommerce.service.CartService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;


import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cs;

    // -------------------- ADD TO CART --------------------

    @Test

    void testAddToCart_Success() throws Exception {

        Cart cart = new Cart();

        Mockito.when(cs.addToCart(anyString(), anyLong(), anyInt()))
                .thenReturn(cart);

        mockMvc.perform(post("/api/cart/add")
                        .with(csrf())
                        .param("productId", "1")
                        .param("quantity", "2"))
                .andExpect(status().isOk());
    }

    // -------------------- GET CART --------------------

    @Test
    void testGetCart_Success() throws Exception {

        Cart cart = new Cart();

        Mockito.when(cs.getCartByUsername(anyString()))
                .thenReturn(cart);

        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk());
    }

    // -------------------- REMOVE ITEM --------------------

    @Test
    void testRemoveItem_Success() throws Exception {

        Cart cart = new Cart();

        Mockito.when(cs.removeItemByUsername(anyString(), anyLong()))
                .thenReturn(cart);

        mockMvc.perform(delete("/api/cart/remove")
                        .with(csrf())
                        .param("productId", "1"))
                .andExpect(status().isOk());
    }

    // -------------------- UPDATE QUANTITY --------------------

    @Test
    void testUpdateQuantity_Success() throws Exception {

        Cart cart = new Cart();

        Mockito.when(cs.updateQuantityByUsername(anyString(), anyLong(), anyInt()))
                .thenReturn(cart);

        mockMvc.perform(put("/api/cart/update")
                        .with(csrf())
                        .param("productId", "1")
                        .param("quantity", "5"))
                .andExpect(status().isOk());
    }

    // -------------------- UNAUTHORIZED --------------------

    @Test
    void testAccessDenied_NoUser() throws Exception {

        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized());
    }

    // -------------------- FORBIDDEN --------------------

    @Test
    void testAccessDenied_WrongRole() throws Exception {

        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isForbidden());
    }
}