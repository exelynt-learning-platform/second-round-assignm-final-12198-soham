package com.mjs.ecommerce.service;

import com.mjs.ecommerce.dto.PaymentRequest;
import com.mjs.ecommerce.dto.PaymentResponse;
import com.mjs.ecommerce.enums.PaymentStatus;
import com.mjs.ecommerce.mapper.PaymentMapper;
import com.mjs.ecommerce.model.Order;
import com.mjs.ecommerce.model.Payment;
import com.mjs.ecommerce.model.User;
import com.mjs.ecommerce.repository.OrderRepo;
import com.mjs.ecommerce.repository.PaymentRepository;
import com.mjs.ecommerce.repository.UserRepository;
import com.stripe.model.PaymentIntent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @InjectMocks
    private PaymentServiceImpl service;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepo orderRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private StripeService stripeService;

    @Test
    void createPayment_success() {

        User user = new User();
        user.setId(1L);

        Order order = new Order();
        order.setId(10L);
        order.setUser(user);

        PaymentRequest request = new PaymentRequest();
        request.setOrderId(10L);
        request.setAmount(100.0);
        request.setCurrency("INR");

        PaymentIntent intent = mock(PaymentIntent.class);
        when(intent.getId()).thenReturn("pi_123");

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setOrder(order);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(stripeService.createPaymentIntent(any())).thenReturn(intent);
        when(paymentMapper.createPayment(any(), any(), any(), any()))
                .thenReturn(payment);

        PaymentResponse response = service.createPayment(1L, request);

        assertNotNull(response);
        verify(paymentRepository).save(payment);
    }



}