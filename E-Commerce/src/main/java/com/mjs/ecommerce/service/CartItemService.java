package com.mjs.ecommerce.service;

import com.mjs.ecommerce.model.CartItem;

import java.util.List;

public interface CartItemService {

    CartItem createCartItem(CartItem cartItem);

    List<CartItem> getAllCartItems();

    CartItem getCartItemById(Long id);

    void deleteCartItem(Long id);
}