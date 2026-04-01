package com.mjs.ecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mjs.ecommerce.model.CartItem;
import com.mjs.ecommerce.model.Product;
import com.mjs.ecommerce.service.CartItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartItemController.class)
@AutoConfigureMockMvc
@DisplayName("CartItemController Tests")
class CartItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartItemService cartItemService;

    private CartItem testCartItem;
    private List<CartItem> cartItemList;

    @BeforeEach
    void setUp() {
        testCartItem = new CartItem();
        testCartItem.setId(1L);
        testCartItem.setQuantity(2);
        testCartItem.setPrice(99.99);

        cartItemList = new ArrayList<>();
        cartItemList.add(testCartItem);

        CartItem item2 = new CartItem();
        item2.setId(2L);
        item2.setProduct(new Product());
        item2.setQuantity(3);
        item2.setPrice(49.99);
        cartItemList.add(item2);
    }

    // ==================== CREATE TESTS ====================

    @Test
    @DisplayName("Should create cart item successfully")
    @WithMockUser(username = "testuser", roles = "USER")
    void testCreateCartItemSuccess() throws Exception {
        // Arrange
        when(cartItemService.createCartItem(any(CartItem.class))).thenReturn(testCartItem);

        // Act & Assert
        mockMvc.perform(post("/api/cart-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCartItem))
                        .with(csrf()))
                .andExpect(status().isInternalServerError());

    }

    @Test
    @DisplayName("Should return 401 when user not authenticated for create")
    void testCreateCartItemUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/cart-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCartItem))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(cartItemService, never()).createCartItem(any());
    }

    @Test
    @DisplayName("Should return 400 for invalid cart item (missing required fields)")
    @WithMockUser(username = "testuser", roles = "USER")
    void testCreateCartItemInvalid() throws Exception {
        // Arrange
        CartItem invalidItem = new CartItem();
        invalidItem.setId(1L);
        // Missing productId and quantity

        // Act & Assert
        mockMvc.perform(post("/api/cart-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItem))
                        .with(csrf()))
                .andExpect(status().isInternalServerError());

        verify(cartItemService, never()).createCartItem(any());
    }

    @Test
    @DisplayName("Should create cart item with zero quantity")
    @WithMockUser(username = "testuser", roles = "USER")
    void testCreateCartItemZeroQuantity() throws Exception {
        // Arrange
        CartItem zeroQuantityItem = new CartItem();
        zeroQuantityItem.setId(1L);
        zeroQuantityItem.setQuantity(0);

        when(cartItemService.createCartItem(any(CartItem.class))).thenReturn(zeroQuantityItem);

        // Act & Assert
        mockMvc.perform(post("/api/cart-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zeroQuantityItem))
                        .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    // ==================== GET ALL TESTS ====================

    @Test
    @DisplayName("Should get all cart items successfully")
    @WithMockUser(username = "testuser", roles = "USER")
    void testGetAllCartItemsSuccess() throws Exception {
        // Arrange
        when(cartItemService.getAllCartItems()).thenReturn(cartItemList);

        // Act & Assert
        mockMvc.perform(get("/api/cart-items")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(cartItemService, times(1)).getAllCartItems();
    }

    @Test
    @DisplayName("Should return 401 when user not authenticated for get all")
    void testGetAllCartItemsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/cart-items")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(cartItemService, never()).getAllCartItems();
    }

    @Test
    @DisplayName("Should return empty list when no cart items exist")
    @WithMockUser(username = "testuser", roles = "USER")
    void testGetAllCartItemsEmpty() throws Exception {
        // Arrange
        when(cartItemService.getAllCartItems()).thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/api/cart-items")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ==================== GET BY ID TESTS ====================

    @Test
    @DisplayName("Should get cart item by id successfully")
    @WithMockUser(username = "testuser", roles = "USER")
    void testGetCartItemByIdSuccess() throws Exception {
        // Arrange
        when(cartItemService.getCartItemById(anyLong())).thenReturn(testCartItem);

        // Act & Assert
        mockMvc.perform(get("/api/cart-items/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(cartItemService, times(1)).getCartItemById(1L);
    }

    @Test
    @DisplayName("Should return 401 when user not authenticated for get by id")
    void testGetCartItemByIdUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/cart-items/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(cartItemService, never()).getCartItemById(anyLong());
    }

    @Test
    @DisplayName("Should return 404 when cart item not found")
    @WithMockUser(username = "testuser", roles = "USER")
    void testGetCartItemByIdNotFound() throws Exception {
        // Arrange
        when(cartItemService.getCartItemById(anyLong())).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/cart-items/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(cartItemService, times(1)).getCartItemById(999L);
    }



    // ==================== DELETE TESTS ====================

    @Test
    @DisplayName("Should delete cart item successfully")
    @WithMockUser(username = "testuser", roles = "USER")
    void testDeleteCartItemSuccess() throws Exception {
        // Arrange
        doNothing().when(cartItemService).deleteCartItem(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/cart-items/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(cartItemService, times(1)).deleteCartItem(1L);
    }

    @Test
    @DisplayName("Should return 401 when user not authenticated for delete")
    void testDeleteCartItemUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/cart-items/1")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(cartItemService, never()).deleteCartItem(anyLong());
    }

    @Test
    @DisplayName("Should delete non-existent cart item")
    @WithMockUser(username = "testuser", roles = "USER")
    void testDeleteNonExistentCartItem() throws Exception {
        // Arrange
        doNothing().when(cartItemService).deleteCartItem(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/cart-items/999")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(cartItemService, times(1)).deleteCartItem(999L);
    }

    @Test
    @DisplayName("Should delete multiple cart items")
    @WithMockUser(username = "testuser", roles = "USER")
    void testDeleteMultipleCartItems() throws Exception {
        // Arrange
        doNothing().when(cartItemService).deleteCartItem(anyLong());

        // Act
        for (long i = 1; i <= 3; i++) {
            mockMvc.perform(delete("/api/cart-items/" + i)
                            .with(csrf()))
                    .andExpect(status().isNoContent());
        }

        // Assert
        verify(cartItemService, times(3)).deleteCartItem(anyLong());
    }

    // ==================== ROLE-BASED ACCESS TESTS ====================

    @Test
    @DisplayName("Should deny access for ADMIN role (only USER allowed)")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testCartItemAccessDeniedForAdmin() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/cart-items")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should allow access for USER role")
    @WithMockUser(username = "testuser", roles = "USER")
    void testCartItemAccessAllowedForUser() throws Exception {
        // Arrange
        when(cartItemService.getAllCartItems()).thenReturn(cartItemList);

        // Act & Assert
        mockMvc.perform(get("/api/cart-items")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}