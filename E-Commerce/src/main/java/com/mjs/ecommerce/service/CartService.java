package com.mjs.ecommerce.service;

import com.mjs.ecommerce.model.Cart;

public interface CartService {
  public  Cart addToCart(String username, Long productId, int quantity);

  public  Cart getCart(Long userId);

  /**
   * Get cart by username (for authenticated users)
   * @param username User email/username
   * @return Cart
   */
  public Cart getCartByUsername(String username);

 public   Cart removeItem(Long userId, Long productId);

 public   Cart updateQuantity(Long userId, Long productId, int quantity);



  /**
   * Remove item from authenticated user's cart
   * @param username User email/username
   * @param productId Product ID to remove
   * @return Updated cart
   */
  public Cart removeItemByUsername(String username, Long productId);

  /**
   * Update quantity in authenticated user's cart
   * @param username User email/username
   * @param productId Product ID
   * @param quantity New quantity
   * @return Updated cart
   */
  public Cart updateQuantityByUsername(String username, Long productId, int quantity);
}
