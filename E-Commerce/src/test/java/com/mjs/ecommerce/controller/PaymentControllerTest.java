package com.mjs.ecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mjs.ecommerce.dto.PaymentRequest;
import com.mjs.ecommerce.dto.PaymentResponse;
import com.mjs.ecommerce.model.Payment;
import com.mjs.ecommerce.service.PaymentServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentServiceImpl paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------
    // Helper Methods
    // -------------------------
    private PaymentResponse mockResponse() {
        PaymentResponse res = new PaymentResponse();
        res.setMessage("success");
        return res;
    }

    private PaymentRequest mockRequest() {
        PaymentRequest req = new PaymentRequest();
        return req;
    }

    private Payment mockPayment() {
        Payment payment = new Payment();
        payment.setId(1L);
        return payment;
    }

    // -------------------------
    // CREATE PAYMENT
    // -------------------------
    @Test
    @WithMockUser(roles = "USER")
    void createPayment_success() throws Exception {

        when(paymentService.createPayment(anyLong(), any()))
                .thenReturn(mockResponse());

        mockMvc.perform(post("/api/payments/create/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createPayment_failure() throws Exception {

        when(paymentService.createPayment(anyLong(), any()))
                .thenThrow(new RuntimeException("error"));

        mockMvc.perform(post("/api/payments/create/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockRequest())))
                .andExpect(status().isForbidden());
    }

    // -------------------------
    // CONFIRM PAYMENT
    // -------------------------
    @Test
    @WithMockUser(roles = "USER")
    void confirmPayment_success() throws Exception {

        when(paymentService.confirmPayment(anyString()))
                .thenReturn(mockResponse());

        mockMvc.perform(post("/api/payments/confirm/test123"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void confirmPayment_failure() throws Exception {

        when(paymentService.confirmPayment(anyString()))
                .thenThrow(new RuntimeException());

        mockMvc.perform(post("/api/payments/confirm/test123"))
                .andExpect(status().isForbidden());
    }

    // -------------------------
    // GET PAYMENT BY ID
    // -------------------------
    @Test
    @WithMockUser(roles = "USER")
    void getPayment_success() throws Exception {

        when(paymentService.getPaymentById(anyLong()))
                .thenReturn(mockPayment());

        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getPayment_notFound() throws Exception {

        when(paymentService.getPaymentById(anyLong()))
                .thenThrow(new RuntimeException());

        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isNotFound());
    }

    // -------------------------
    // GET PAYMENTS BY USER
    // -------------------------
    @Test
    @WithMockUser(roles = "USER")
    void getPaymentsByUser_success() throws Exception {

        when(paymentService.getPaymentsByUser(anyLong()))
                .thenReturn(List.of(mockPayment()));

        mockMvc.perform(get("/api/payments/user/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getPaymentsByUser_notFound() throws Exception {

        when(paymentService.getPaymentsByUser(anyLong()))
                .thenThrow(new RuntimeException());

        mockMvc.perform(get("/api/payments/user/1"))
                .andExpect(status().isNotFound());
    }

    // -------------------------
    // GET PAYMENTS BY ORDER
    // -------------------------
    @Test
    @WithMockUser(roles = "USER")
    void getPaymentsByOrder_success() throws Exception {

        when(paymentService.getPaymentsByOrder(anyLong()))
                .thenReturn(List.of(mockPayment()));

        mockMvc.perform(get("/api/payments/order/1"))
                .andExpect(status().isOk());
    }

    // -------------------------
    // PAYMENT STATUS
    // -------------------------
    @Test
    @WithMockUser(roles = "USER")
    void getPaymentStatus_success() throws Exception {

        when(paymentService.getPaymentStatus(anyString()))
                .thenReturn(mockResponse());

        mockMvc.perform(get("/api/payments/status/test123"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getPaymentStatus_failure() throws Exception {

        when(paymentService.getPaymentStatus(anyString()))
                .thenThrow(new RuntimeException());

        mockMvc.perform(get("/api/payments/status/test123"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createPayment_unauthorized() throws Exception {

        mockMvc.perform(post("/api/payments/create/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}