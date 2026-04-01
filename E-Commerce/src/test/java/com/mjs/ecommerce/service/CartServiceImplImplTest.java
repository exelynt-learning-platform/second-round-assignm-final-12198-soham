package com.mjs.ecommerce.service;

import com.mjs.ecommerce.constants.Constants;
import com.mjs.ecommerce.model.Cart;
import com.mjs.ecommerce.model.CartItem;
import com.mjs.ecommerce.model.Product;
import com.mjs.ecommerce.model.User;
import com.mjs.ecommerce.repository.CartRepo;
import com.mjs.ecommerce.repository.ProductRepository;
import com.mjs.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartServiceImpl Tests")
class CartServiceImplTest {

    @Mock
    private CartRepo cartRepo;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private User testUser;
    private Product testProduct;
    private Cart testCart;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");

        // Setup test product
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(99.99);
        testProduct.setStockQuantity(10);

        // Setup test cart
        testCart = new Cart();
        testCart.setId(1L);
        testCart.setUser(testUser);
        testCart.setItems(new ArrayList<>());

        // Setup test cart item
        testCartItem = new CartItem();
        testCartItem.setId(1L);
        testCartItem.setProduct(testProduct);
        testCartItem.setQuantity(2);
        testCartItem.setPrice(99.99);
    }

    // ==================== ADD TO CART TESTS ====================

    @Test
    @DisplayName("Should add new product to cart successfully")
    void testAddToCartSuccess() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartRepo.save(any(Cart.class))).thenReturn(testCart);

        // Act
        Cart result = cartService.addToCart("test@example.com", 1L, 2);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(2, result.getItems().get(0).getQuantity());
        verify(cartRepo, times(1)).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should create new cart if not exists")
    void testAddToCartCreatesNewCart() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.empty());
        when(cartRepo.save(any(Cart.class))).thenReturn(testCart);

        // Act
        Cart result = cartService.addToCart("test@example.com", 1L, 2);

        // Assert
        assertNotNull(result);
        verify(cartRepo, times(2)).save(any(Cart.class)); // Once for create, once for add item
    }

    @Test
    @DisplayName("Should increase quantity if product already in cart")
    void testAddToCartIncreaseQuantity() {
        // Arrange
        testCart.getItems().add(testCartItem);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartRepo.save(any(Cart.class))).thenReturn(testCart);

        // Act
        Cart result = cartService.addToCart("test@example.com", 1L, 3);

        // Assert
        assertEquals(1, result.getItems().size());
        assertEquals(5, result.getItems().get(0).getQuantity()); // 2 + 3
        verify(cartRepo, times(1)).save(any(Cart.class));
    }



    @Test
    @DisplayName("Should throw exception when user not found")
    void testAddToCartUserNotFound() {
        // Arrange
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cartService.addToCart("unknown@example.com", 1L, 2)
        );
        assertEquals(Constants.USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void testAddToCartProductNotFound() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cartService.addToCart("test@example.com", 999L, 2)
        );
        assertEquals(Constants.PRODUCT_NOT_FOUND, exception.getMessage());
    }

    @Test
    @DisplayName("FIX #2: Should throw exception when requested quantity exceeds stock")
    void testAddToCartExceedsStock() {
        // Arrange
        testProduct.setStockQuantity(5);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(testCart));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cartService.addToCart("test@example.com", 1L, 10)
        );
        assertTrue(exception.getMessage().contains("exceeds available stock"));
    }

    @Test
    @DisplayName("FIX #1: Should handle null items list when adding to cart")
    void testAddToCartNullItems() {
        // Arrange
        testCart.setItems(null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartRepo.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart cart = invocation.getArgument(0);
            if (cart.getItems() == null) {
                cart.setItems(new ArrayList<>());
            }
            cart.getItems().add(testCartItem);
            return cart;
        });

        // Act
        Cart result = cartService.addToCart("test@example.com", 1L, 2);

        // Assert
        assertNotNull(result.getItems());
        verify(cartRepo, times(1)).save(any(Cart.class));
    }

    // ==================== GET CART TESTS ====================

    @Test
    @DisplayName("Should get cart by userId successfully")
    void testGetCartByUserIdSuccess() {
        // Arrange
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(testCart));

        // Act
        Cart result = cartService.getCart(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(cartRepo, times(1)).findByUserId(1L);
    }

    @Test
    @DisplayName("Should throw exception when cart not found by userId")
    void testGetCartByUserIdNotFound() {
        // Arrange
        when(cartRepo.findByUserId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cartService.getCart(999L)
        );
        assertEquals(Constants.CART_NOT_FOUND, exception.getMessage());
    }

    @Test
    @DisplayName("Should get cart by username successfully")
    void testGetCartByUsernameSuccess() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(testCart));

        // Act
        Cart result = cartService.getCartByUsername("test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("Should throw exception when user not found for getCartByUsername")
    void testGetCartByUsernameUserNotFound() {
        // Arrange
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> cartService.getCartByUsername("unknown@example.com")
        );
    }

    // ==================== UPDATE QUANTITY TESTS ====================

    @Test
    @DisplayName("FIX #4: Should update quantity by userId with validation")
    void testUpdateQuantityByUserIdSuccess() {
        // Arrange
        testCart.getItems().add(testCartItem);
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartRepo.save(any(Cart.class))).thenReturn(testCart);

        // Act
        Cart result = cartService.updateQuantity(1L, 1L, 5);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.getItems().get(0).getQuantity());
        verify(cartRepo, times(1)).save(any(Cart.class));
    }

    @Test
    @DisplayName("FIX #4: Should update quantity by username with validation")
    void testUpdateQuantityByUsernameSuccess() {
        // Arrange
        testCart.getItems().add(testCartItem);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartRepo.save(any(Cart.class))).thenReturn(testCart);

        // Act
        Cart result = cartService.updateQuantityByUsername("test@example.com", 1L, 3);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getItems().get(0).getQuantity());
    }

    @Test
    @DisplayName("Should throw exception when updating with invalid quantity")
    void testUpdateQuantityInvalidQuantity() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> cartService.updateQuantity(1L, 1L, 0)
        );
    }

    @Test
    @DisplayName("FIX #1: Should throw exception when updating items on null list")
    void testUpdateQuantityNullItems() {
        // Arrange
        testCart.setItems(null);
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(testCart));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cartService.updateQuantity(1L, 1L, 5)
        );
        assertEquals(Constants.PRODUCT_NOT_FOUND, exception.getMessage());
    }

    @Test
    @DisplayName("FIX #2: Should throw exception when update exceeds stock")
    void testUpdateQuantityExceedsStock() {
        // Arrange
        testProduct.setStockQuantity(3);
        testCartItem.setProduct(testProduct);
        testCart.getItems().add(testCartItem);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(testCart));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cartService.updateQuantityByUsername("test@example.com", 1L, 10)
        );
        assertTrue(exception.getMessage().contains("exceeds available stock"));
    }

    @Test
    @DisplayName("Should throw exception when product not found during update")
    void testUpdateQuantityProductNotFound() {
        // Arrange
        testCart.setItems(new ArrayList<>());
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(testCart));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cartService.updateQuantity(1L, 999L, 5)
        );
        assertEquals(Constants.PRODUCT_NOT_FOUND, exception.getMessage());
    }

    // ==================== REMOVE ITEM TESTS ====================

    @Test
    @DisplayName("FIX #3: Should remove item from cart successfully")
    void testRemoveItemSuccess() {
        // Arrange
        testCart.getItems().add(testCartItem);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartRepo.save(any(Cart.class))).thenReturn(testCart);

        // Act
        Cart result = cartService.removeItemByUsername("test@example.com", 1L);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getItems().size());
        verify(cartRepo, times(1)).save(any(Cart.class));
    }

    @Test
    @DisplayName("FIX #3: Should throw exception when removing non-existent product")
    void testRemoveItemProductNotFound() {
        // Arrange
        testCart.getItems().add(testCartItem);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(testCart));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cartService.removeItemByUsername("test@example.com", 999L)
        );
        assertTrue(exception.getMessage().contains("not found in cart"));
    }

    @Test
    @DisplayName("FIX #1: Should throw exception when removing from empty cart")
    void testRemoveItemEmptyCart() {
        // Arrange
        testCart.setItems(new ArrayList<>());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(testCart));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cartService.removeItemByUsername("test@example.com", 1L)
        );
        assertEquals(Constants.CART_ITEM_IS_NULL, exception.getMessage());
    }

    @Test
    @DisplayName("FIX #1: Should throw exception when removing from null items list")
    void testRemoveItemNullItems() {
        // Arrange
        testCart.setItems(null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(testCart));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cartService.removeItemByUsername("test@example.com", 1L)
        );
        assertEquals(Constants.CART_ITEM_IS_NULL, exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when user not found for removal")
    void testRemoveItemUserNotFound() {
        // Arrange
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> cartService.removeItemByUsername("unknown@example.com", 1L)
        );
    }

    // ==================== BONUS UTILITY TESTS ====================

    @Test
    @DisplayName("Should clear entire cart")
    void testClearCart() {
        // Arrange
        testCart.getItems().add(testCartItem);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartRepo.save(any(Cart.class))).thenReturn(testCart);

        // Act
        Cart result = cartService.clearCart("test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getItems().size());
        verify(cartRepo, times(1)).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should calculate cart total correctly")
    void testGetCartTotal() {
        // Arrange
        testCart.getItems().add(testCartItem); // 99.99 * 2 = 199.98
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(testCart));

        // Act
        double total = cartService.getCartTotal("test@example.com");

        // Assert
        assertEquals(199.98, total, 0.01);
    }

    @Test
    @DisplayName("Should return zero total for empty cart")
    void testGetCartTotalEmpty() {
        // Arrange
        testCart.setItems(new ArrayList<>());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(testCart));

        // Act
        double total = cartService.getCartTotal("test@example.com");

        // Assert
        assertEquals(0.0, total);
    }

    @Test
    @DisplayName("Should get correct item count")
    void testGetCartItemCount() {
        // Arrange
        testCart.getItems().add(testCartItem); // quantity = 2
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(testCart));

        // Act
        int count = cartService.getCartItemCount("test@example.com");

        // Assert
        assertEquals(2, count);
    }

    @Test
    @DisplayName("Should return zero count for empty cart")
    void testGetCartItemCountEmpty() {
        // Arrange
        testCart.setItems(new ArrayList<>());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(testCart));

        // Act
        int count = cartService.getCartItemCount("test@example.com");

        // Assert
        assertEquals(0, count);
    }

    @Test
    @DisplayName("Should return zero count when items is null")
    void testGetCartItemCountNullItems() {
        // Arrange
        testCart.setItems(null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartRepo.findByUserId(1L)).thenReturn(Optional.of(testCart));

        // Act
        int count = cartService.getCartItemCount("test@example.com");

        // Assert
        assertEquals(0, count);
    }
}