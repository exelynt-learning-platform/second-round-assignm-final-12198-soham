package com.mjs.ecommerce.repository;

import com.mjs.ecommerce.enums.OrderStatus;
import com.mjs.ecommerce.enums.PaymentStatus;
import com.mjs.ecommerce.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find all orders for a specific user
     * @param userId User ID
     * @return List of orders belonging to the user
     */
    List<Order> findByUserId(Long userId);

    /**
     * Find orders by status
     * @param status Order status
     * @return List of orders with specified status
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Find orders by payment status
     * @param paymentStatus Payment status
     * @return List of orders with specified payment status
     */
    List<Order> findByPaymentStatus(PaymentStatus paymentStatus);

    /**
     * Find orders for a user with specific status
     * @param userId User ID
     * @param status Order status
     * @return List of orders matching both criteria
     */
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    /**
     * Find orders for a user with specific payment status
     * @param userId User ID
     * @param paymentStatus Payment status
     * @return List of orders matching both criteria
     */
    List<Order> findByUserIdAndPaymentStatus(Long userId, PaymentStatus paymentStatus);

    /**
     * Find orders by both order status and payment status
     * @param orderStatus Order status
     * @param paymentStatus Payment status
     * @return List of orders matching both criteria
     */
    List<Order> findByStatusAndPaymentStatus(OrderStatus orderStatus, PaymentStatus paymentStatus);

    /**
     * Find orders for a user with both statuses
     * @param userId User ID
     * @param orderStatus Order status
     * @param paymentStatus Payment status
     * @return List of orders matching all criteria
     */
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status = :orderStatus AND o.paymentStatus = :paymentStatus")
    List<Order> findByUserIdAndStatusAndPaymentStatus(
            @Param("userId") Long userId,
            @Param("orderStatus") OrderStatus orderStatus,
            @Param("paymentStatus") PaymentStatus paymentStatus
    );


    /**
     * Count orders for a user
     * @param userId User ID
     * @return Number of orders for the user
     */
    long countByUserId(Long userId);

    /**
     * Count orders by status
     * @param status Order status
     * @return Number of orders with specified status
     */
    long countByStatus(OrderStatus status);

    /**
     * Count pending orders (not paid)
     * @param paymentStatus Payment status
     * @return Number of pending orders
     */
    long countByPaymentStatus(PaymentStatus paymentStatus);

    /**
     * Check if an order exists for a user
     * @param orderId Order ID
     * @param userId User ID
     * @return true if order belongs to user, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Order o WHERE o.id = :orderId AND o.user.id = :userId")
    boolean existsByIdAndUserId(@Param("orderId") Long orderId, @Param("userId") Long userId);

    /**
     * Find the latest order for a user
     * @param userId User ID
     * @return Optional containing the latest order or empty
     */
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.id DESC LIMIT 1")
    Optional<Order> findLatestOrderByUserId(@Param("userId") Long userId);

    /**
     * Find all pending orders that need payment
     * @return List of orders with PENDING payment status
     */
    @Query("SELECT o FROM Order o WHERE o.paymentStatus = 'PENDING'")
    List<Order> findAllPendingOrders();

    /**
     * Find all completed orders
     * @return List of orders with COMPLETED status
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'COMPLETED'")
    List<Order> findAllCompletedOrders();

    /**
     * Find orders with specific shipping address
     * @param shippingAddress Shipping address to search for
     * @return List of orders with matching shipping address
     */
    List<Order> findByShippingAddressContainingIgnoreCase(String shippingAddress);

    /**
     * Find orders for a user with price greater than amount
     * @param userId User ID
     * @param minPrice Minimum price threshold
     * @return List of orders with price greater than minPrice
     */
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.totalPrice > :minPrice")
    List<Order> findByUserIdAndPriceGreaterThan(@Param("userId") Long userId, @Param("minPrice") double minPrice);

    /**
     * Find orders for a user within a price range
     * @param userId User ID
     * @param minPrice Minimum price
     * @param maxPrice Maximum price
     * @return List of orders within the price range
     */
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.totalPrice BETWEEN :minPrice AND :maxPrice")
    List<Order> findByUserIdAndPriceBetween(
            @Param("userId") Long userId,
            @Param("minPrice") double minPrice,
            @Param("maxPrice") double maxPrice
    );

    /**
     * Delete all orders for a user
     * @param userId User ID
     */
    void deleteByUserId(Long userId);

    /**
     * Delete orders by status
     * @param status Order status to delete
     */
    void deleteByStatus(OrderStatus status);
}

