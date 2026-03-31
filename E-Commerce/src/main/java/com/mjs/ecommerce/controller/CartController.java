package com.mjs.ecommerce.controller;

import com.mjs.ecommerce.model.Cart;
import com.mjs.ecommerce.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cs;


    @PostMapping("/add")
    public ResponseEntity<Cart> addToCart(@RequestParam Long productId,
                                          @RequestParam int quantity,
                                          @AuthenticationPrincipal UserDetails user) {
        String username = user.getUsername();
        Cart cart = cs.addToCart(username, productId, quantity);
        return ResponseEntity.ok(cart);
    }

    @GetMapping
    public ResponseEntity<Cart> getCart(@AuthenticationPrincipal UserDetails user) {
        String username = user.getUsername();
        Cart cart = cs.getCartByUsername(username);
        return ResponseEntity.ok(cart);
    }


    @DeleteMapping("/remove")
    public ResponseEntity<Cart> removeItem(@RequestParam Long productId,
                                           @AuthenticationPrincipal UserDetails user) {
        String username = user.getUsername();
        Cart cart = cs.removeItemByUsername(username, productId);
        return ResponseEntity.ok(cart);
    }



    @PutMapping("/update")
    public ResponseEntity<Cart> updateQuantity(@RequestParam Long productId,
                                               @RequestParam int quantity,
                                               @AuthenticationPrincipal UserDetails user) {
        String username = user.getUsername();
        Cart cart = cs.updateQuantityByUsername(username, productId, quantity);
        return ResponseEntity.ok(cart);
    }
}