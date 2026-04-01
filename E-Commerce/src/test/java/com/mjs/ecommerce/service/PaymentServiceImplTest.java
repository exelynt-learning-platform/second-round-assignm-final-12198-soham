package com.mjs.ecommerce.service;

import com.mjs.ecommerce.dto.PaymentRequest;
import com.mjs.ecommerce.enums.PaymentStatus;
import com.mjs.ecommerce.model.Order;
import com.mjs.ecommerce.model.Payment;
import com.mjs.ecommerce.model.User;
import com.mjs.ecommerce.repository.OrderRepo;
import com.mjs.ecommerce.repository.PaymentRepository;
import com.mjs.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@org.junit.jupiter.api.extension.ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @InjectMocks
    private PaymentServiceImpl service;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepo orderRepository;

    private User user;
    private Order order;
    private PaymentRequest request;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);

        order = new Order();
        order.setId(10L);
        order.setUser(user);

        request = new PaymentRequest();
        request.setOrderId(10L);
        request.setAmount(100.0);
        request.setCurrency("INR");
        request.setDescription("Test payment");
    }



    @Test
    void createPayment_userNotFound() {

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.createPayment(1L, request));
    }

    // -------------------------
    // ORDER NOT FOUND
    // -------------------------
    @Test
    void createPayment_orderNotFound() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.createPayment(1L, request));
    }

    // -------------------------
    // INVALID AMOUNT
    // -------------------------
    @Test
    void createPayment_invalidAmount() {

        request.setAmount(100.123);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThrows(RuntimeException.class,
                () -> service.createPayment(1L, request));
    }

    // -------------------------
    // GET PAYMENT BY ID
    // -------------------------
    @Test
    void getPaymentById_success() {

        Payment payment = new Payment();
        payment.setId(1L);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        Payment result = service.getPaymentById(1L);

        assertNotNull(result);
    }

    @Test
    void getPaymentById_notFound() {

        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.getPaymentById(1L));
    }




    // -------------------------
    // REFUND PAYMENT SUCCESS
    // -------------------------


    // -------------------------
    // REFUND FAILURE
    // -------------------------
    @Test
    void refundPayment_invalidStatus() {

        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        assertThrows(RuntimeException.class,
                () -> service.refundPayment(1L));
    }


}