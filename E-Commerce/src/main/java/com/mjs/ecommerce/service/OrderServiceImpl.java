package com.mjs.ecommerce.service;

import com.mjs.ecommerce.constants.Constants;
import com.mjs.ecommerce.Exception.OutOfStockException;
import com.mjs.ecommerce.enums.OrderStatus;
import com.mjs.ecommerce.enums.PaymentStatus;
import com.mjs.ecommerce.model.*;
import com.mjs.ecommerce.repository.CartRepo;
import com.mjs.ecommerce.repository.OrderRepo;
import com.mjs.ecommerce.repository.ProductRepository;
import com.mjs.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    private static final int MIN_STOCK_THRESHOLD = 2;

    @Autowired
    private OrderRepo orp;

    @Autowired
    private CartRepo crp;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ProductRepository repository;

    @Override
    public Order createOrder(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException(Constants.USER_NOT_FOUND));

        Cart cart = crp.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException(Constants.CART_NOT_FOUND));

        validateCart(cart);

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setPaymentStatus(PaymentStatus.PENDING);

        List<OrderItem> orderItems = buildOrderItems(cart, order); // ✅ Extracted
        double total = calculateTotal(orderItems);                  // ✅ Extracted

        order.setItems(orderItems);
        order.setTotalPrice(total);

        clearCart(cart);

        return orp.save(order);
    }

    private void validateCart(Cart cart) {
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot create order from empty cart. Please add items first.");
        }
    }

    private List<OrderItem> buildOrderItems(Cart cart, Order order) {
        List<OrderItem> orderItems = new ArrayList<>();
        if (CollectionUtils.isEmpty(cart.getItems())) {
            throw new IllegalArgumentException("Cart is empty. Cannot create order.");
        } else {
            for (CartItem ci : cart.getItems()) {
                Product product = repository.findById(ci.getProduct().getId())
                        .orElseThrow(() -> new RuntimeException(Constants.PRODUCT_NOT_FOUND));

                validateStock(product, ci.getQuantity()); // ✅ Extracted

                OrderItem oi = new OrderItem();
                oi.setOrder(order);
                oi.setProduct(product);
                oi.setQuantity(ci.getQuantity());
                oi.setPrice(product.getPrice());

                product.setStockQuantity(product.getStockQuantity() - ci.getQuantity());
                repository.save(product);

                orderItems.add(oi);
            }

            return orderItems;
        }
    }

    private void validateStock(Product product, int requestedQuantity) {
        int availableStock = product.getStockQuantity();

        if (availableStock - requestedQuantity < MIN_STOCK_THRESHOLD) {
            throw new OutOfStockException(
                    "Insufficient stock for product: " + product.getName()
            );
        }
    }

    private double calculateTotal(List<OrderItem> orderItems) {
        return orderItems.stream()
                .mapToDouble(oi -> oi.getPrice() * oi.getQuantity())
                .sum();
    }

    private void clearCart(Cart cart) {
        cart.getItems().clear();
        crp.save(cart);
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

    @Override
    public Order createOrderByUsername(String username) {
        User user = userRepo.findByEmail(username)
                .orElseThrow(() -> new RuntimeException(Constants.USER_NOT_FOUND));

        return createOrder(user.getId());
    }
}