package com.mjs.ecommerce.service;

import com.mjs.ecommerce.Constants;
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

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

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

        // 1. Get User
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException(Constants.USER_NOT_FOUND));

        // 2. Get Cart
        Cart cart = crp.findByUserId(userId).orElseThrow(() -> new RuntimeException("Cart not found"));


        // 3. Validate cart is not empty
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot create order from empty cart. Please add items to cart first.");
        }

        // 4. Create Order
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setPaymentStatus(PaymentStatus.PENDING);
        List<OrderItem> orderItems = new ArrayList<>();

        double total = 0;


      for (CartItem ci : cart.getItems()) {

            Product product = repository.findById(ci.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException(Constants.PRODUCT_NOT_FOUND));

            if (product.getStockQuantity() < ci.getQuantity()) {
                throw new OutOfStockException("Product out of stock: " + product.getName());
            }

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(product);
            oi.setQuantity(ci.getQuantity());
            oi.setPrice(product.getPrice());

            total += product.getPrice() * ci.getQuantity();

            product.setStockQuantity(product.getStockQuantity() - ci.getQuantity());
            repository.save(product);

            orderItems.add(oi);
        }

        // 6. Set items & total
        order.setItems(orderItems);
        order.setTotalPrice(total);

        // 7. Clear cart
        cart.getItems().clear();
        crp.save(cart);

        // 8. Save order
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

    @Override
    public Order createOrderByUsername(String username) {

        User user = userRepo.findByEmail(username)
                .orElseThrow(() -> new RuntimeException(Constants.USER_NOT_FOUND));

        return createOrder(user.getId());
    }
}