package com.mjs.ecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mjs.ecommerce.model.Order;
import com.mjs.ecommerce.service.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderServiceImpl service;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------
    // Helper Method
    // -------------------------
    private Order mockOrder() {
        Order order = new Order();
        order.setId(1L);
        return order;
    }

    // -------------------------
    // CREATE ORDER
    // -------------------------
    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void createOrder_success() throws Exception {

        when(service.createOrderByUsername(anyString()))
                .thenReturn(mockOrder());

        mockMvc.perform(post("/api/orders/create"))
                .andExpect(status().isForbidden());
    }

    // -------------------------
    // GET ORDERS BY USER
    // -------------------------
    @Test
    @WithMockUser(roles = {"USER"})
    void getOrders_success() throws Exception {

        when(service.getOrdersByUser(anyLong()))
                .thenReturn(List.of(mockOrder()));

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk());
    }

    // -------------------------
    // GET ORDER BY ID
    // -------------------------
    @Test
    @WithMockUser(roles = {"USER"})
    void getOrderById_success() throws Exception {

        when(service.getOrderById(anyLong()))
                .thenReturn(mockOrder());

        mockMvc.perform(get("/api/orders/details/1"))
                .andExpect(status().isOk());
    }

    // -------------------------
    // UNAUTHORIZED (No User)
    // -------------------------
    @Test
    void createOrder_unauthorized() throws Exception {

        mockMvc.perform(post("/api/orders/create"))
                .andExpect(status().isForbidden());
    }

    // -------------------------
    // FORBIDDEN (Wrong Role)
    // -------------------------
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createOrder_wrongRole() throws Exception {

        mockMvc.perform(post("/api/orders/create"))
                .andExpect(status().isForbidden());
    }
}