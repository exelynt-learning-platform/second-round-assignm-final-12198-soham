package com.mjs.ecommerce.service;

import com.mjs.ecommerce.exception.CartItemNotFoundException;
import com.mjs.ecommerce.exception.CartNotFoundException;
import com.mjs.ecommerce.exception.OrderNotFoundException;
import com.mjs.ecommerce.exception.OutOfStockException;
import com.mjs.ecommerce.exception.ProductNotFoundException;
import com.mjs.ecommerce.exception.UserNotFoundException;
import com.mjs.ecommerce.enums.OrderStatus;
import com.mjs.ecommerce.enums.PaymentStatus;
import com.mjs.ecommerce.model.Cart;
import com.mjs.ecommerce.model.CartItem;
import com.mjs.ecommerce.model.Order;
import com.mjs.ecommerce.model.OrderItem;
import com.mjs.ecommerce.model.Product;
import com.mjs.ecommerce.model.User;
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

    /**
     * Business rule:
     * after a successful order, at least 2 units must remain in stock.
     */
    private static final int MIN_REMAINING_STOCK_AFTER_ORDER = 2;

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
                .orElseThrow(UserNotFoundException::new);

        Cart cart = crp.findByUserId(userId)
                .orElseThrow(CartNotFoundException::new);

        validateCart(cart);

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setPaymentStatus(PaymentStatus.PENDING);

        List<OrderItem> orderItems = buildOrderItems(cart, order);
        double total = calculateTotal(orderItems);

        order.setItems(orderItems);
        order.setTotalPrice(total);

        clearCart(cart);

        return orp.save(order);
    }

    @Override
    public List<Order> getOrdersByUser(Long userId) {
        return orp.findByUserId(userId);
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orp.findById(orderId)
                .orElseThrow(OrderNotFoundException::new);
    }

    @Override
    public Order createOrderByUsername(String username) {
        User user = userRepo.findByEmail(username)
                .orElseThrow(UserNotFoundException::new);

        return createOrder(user.getId());
    }

    private void validateCart(Cart cart) {
        if (cart == null || CollectionUtils.isEmpty(cart.getItems())) {
            throw new CartItemNotFoundException(
                    "Cannot create order from empty cart. Please add items first."
            );
        }
    }

    private List<OrderItem> buildOrderItems(Cart cart, Order order) {
        List<CartItem> cartItems = cart.getItems();

        if (CollectionUtils.isEmpty(cartItems)) {
            throw new CartItemNotFoundException("Cart is empty. Cannot create order.");
        }

        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem ci : cartItems) {
            Product product = repository.findById(ci.getProduct().getId())
                    .orElseThrow(ProductNotFoundException::new);

            validateStock(product, ci.getQuantity());

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

    private void validateStock(Product product, int requestedQuantity) {
        int availableStock = product.getStockQuantity();

        if (availableStock - requestedQuantity < MIN_REMAINING_STOCK_AFTER_ORDER) {
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
        if (!CollectionUtils.isEmpty(cart.getItems())) {
            cart.getItems().clear();
        }
        crp.save(cart);
    }
}