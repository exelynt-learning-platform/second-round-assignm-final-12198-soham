package com.mjs.ecommerce.service;

import com.mjs.ecommerce.model.Cart;

public interface CartServiceI {
  public  Cart addToCart(Long userId, Long productId, int quantity);

  public  Cart getCart(Long userId);

 public   Cart removeItem(Long userId, Long productId);

 public   Cart updateQuantity(Long userId, Long productId, int quantity);

   public void removeall();
}
