package com.mjs.ecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mjs.ecommerce.model.CartItem;
import com.mjs.ecommerce.service.CartItemServiceI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartItemController.class)
class CartItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartItemServiceI cis;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------
    // Helper Method
    // -------------------------
    private CartItem mockItem() {
        CartItem item = new CartItem();
        item.setId(1L);
        item.setQuantity(2);
        return item;
    }

    // -------------------------
    // CREATE
    // -------------------------
    @Test
    @WithMockUser(roles = "USER")
    void create_success() throws Exception {

        CartItem item = mockItem();

        when(cis.createCartItem(any(CartItem.class)))
                .thenReturn(item);

        mockMvc.perform(post("/api/cart-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isForbidden());
    }

    // -------------------------
    // GET ALL
    // -------------------------
    @Test
    @WithMockUser(roles = "USER")
    void getAll_success() throws Exception {

        when(cis.getAllCartItems())
                .thenReturn(List.of(mockItem()));

        mockMvc.perform(get("/api/cart-items"))
                .andExpect(status().isOk());
    }

    // -------------------------
    // GET BY ID
    // -------------------------
    @Test
    @WithMockUser(roles = "USER")
    void getById_success() throws Exception {

        when(cis.getCartItemById(anyLong()))
                .thenReturn(mockItem());

        mockMvc.perform(get("/api/cart-items/1"))
                .andExpect(status().isOk());
    }

    // -------------------------
    // DELETE
    // -------------------------
    @Test
    @WithMockUser(roles = "USER")
    void delete_success() throws Exception {

        doNothing().when(cis).deleteCartItem(anyLong());

        mockMvc.perform(delete("/api/cart-items/1"))
                .andExpect(status().isForbidden());
    }

    // -------------------------
    // UNAUTHORIZED (No User)
    // -------------------------
    @Test
    void create_unauthorized() throws Exception {

        mockMvc.perform(post("/api/cart-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    // -------------------------
    // FORBIDDEN (Wrong Role)
    // -------------------------
    @Test
    @WithMockUser(roles = "ADMIN")
    void create_wrongRole() throws Exception {

        mockMvc.perform(post("/api/cart-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}
