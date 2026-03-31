package com.mjs.ecommerce.service;

import com.mjs.ecommerce.Constants;
import com.mjs.ecommerce.enums.OrderStatus;
import com.mjs.ecommerce.enums.PaymentStatus;
import com.mjs.ecommerce.model.*;
import com.mjs.ecommerce.repository.CartRepo;
import com.mjs.ecommerce.repository.OrderRepo;
import com.mjs.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderServiceI {

    @Autowired
    private OrderRepo orp;

    @Autowired
    private CartRepo crp;

    @Autowired
    private UserRepository userRepo;

    @Override
    public Order createOrder(Long userId) {

        // 1. Get User
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException(Constants.USER_NOT_FOUND));

        // 2. Get Cart
        Cart cart = crp.findByUserId(userId).orElseThrow(() -> new RuntimeException("Cart not found"));

        // 3. Create Order
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setPaymentStatus(PaymentStatus.PENDING);
        List<OrderItem> orderItems = new ArrayList<>();

        double total = 0;

        // 4. Convert CartItems → OrderItems
        for (CartItem ci : cart.getItems()) {

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(ci.getProduct());
            oi.setQuantity(ci.getQuantity());
            oi.setPrice(ci.getProduct().getPrice());

            total += ci.getProduct().getPrice() * ci.getQuantity();

            orderItems.add(oi);
        }

        // 5. Set items & total
        order.setItems(orderItems);
        order.setTotalPrice(total);

        // 6. Clear cart
        cart.getItems().clear();
        crp.save(cart);

        // 7. Save order
        return orp.save(order);
    }

    @Override
    public List<Order> getOrdersByUser(Long userId) {
        return orp.findByUserId(userId);
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orp.findById(orderId)
                .orElseThrow(() -> new RuntimeException(Constants.ORDER_NOT_FOUND));
    }
}