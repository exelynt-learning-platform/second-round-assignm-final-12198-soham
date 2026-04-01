package com.mjs.ecommerce.controller;

import com.mjs.ecommerce.model.Cart;
import com.mjs.ecommerce.service.CartService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;


import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;


@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cs;

    @Autowired
    private ObjectMapper objectMapper;

    // --------------------------
    // Helper Method
    // --------------------------
    private Cart mockCart() {
        Cart cart = new Cart();
        cart.setId(100L);
        return cart;
    }

    // --------------------------
    // ADD TO CART
    // --------------------------
    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void testAddToCart() throws Exception {

        Cart cart = mockCart();

        when(cs.addToCart(anyString(), anyLong(), anyInt()))
                .thenReturn(cart);

        mockMvc.perform(post("/api/cart/add")
                        .param("productId", "1")
                        .param("quantity", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));
    }

    // --------------------------
    // GET CART
    // --------------------------
    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void testGetCart() throws Exception {

        Cart cart = mockCart();

        when(cs.getCartByUsername(anyString()))
                .thenReturn(cart);

        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));
    }

    // --------------------------
    // REMOVE ITEM
    // --------------------------
    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void testRemoveItem() throws Exception {

        Cart cart = mockCart();

        when(cs.removeItemByUsername(anyString(), anyLong()))
                .thenReturn(cart);

        mockMvc.perform(delete("/api/cart/remove")
                        .param("productId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));
    }

    // --------------------------
    // UPDATE QUANTITY
    // --------------------------
    @Test
    @WithMockUser(username = "user1", roles = {"USER"})
    void testUpdateQuantity() throws Exception {

        Cart cart = mockCart();

        when(cs.updateQuantityByUsername(anyString(), anyLong(), anyInt()))
                .thenReturn(cart);

        mockMvc.perform(put("/api/cart/update")
                        .param("productId", "1")
                        .param("quantity", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));
    }

    // --------------------------
    // NEGATIVE TEST: UNAUTHORIZED
    // --------------------------
    @Test
    void testAddToCart_Unauthorized() throws Exception {

        mockMvc.perform(post("/api/cart/add")
                        .param("productId", "1")
                        .param("quantity", "2"))
                .andExpect(status().isUnauthorized());
    }


}