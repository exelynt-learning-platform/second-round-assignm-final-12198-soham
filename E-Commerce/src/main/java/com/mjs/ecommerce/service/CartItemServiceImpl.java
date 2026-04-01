package com.mjs.ecommerce.service;

import com.mjs.ecommerce.constants.Constants;
import com.mjs.ecommerce.model.CartItem;
import com.mjs.ecommerce.repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class CartItemServiceImpl implements CartItemService {
    @Autowired
    private CartItemRepository cir;

    @Override
    public CartItem createCartItem(CartItem cartItem) {
        return cir.save(cartItem);
    }

    @Override
    public List<CartItem> getAllCartItems() {
        return cir.findAll();
    }

    @Override
    public CartItem getCartItemById(Long id) {
        return cir.findById(id)
                .orElseThrow(() -> new RuntimeException(Constants.CART_NOT_FOUND));
    }

    @Override
    public void deleteCartItem(Long id) {

        CartItem item = cir.findById(id)
                .orElseThrow(() -> new RuntimeException(Constants.CART_NOT_FOUND));

        cir.delete(item);
    }
}