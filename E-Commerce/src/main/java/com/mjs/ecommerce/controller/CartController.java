package com.mjs.ecommerce.controller;

import com.mjs.ecommerce.model.Cart;
import com.mjs.ecommerce.service.CartService;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Validated
public class CartController {

    @Autowired
    private CartService cartService;    


    @PostMapping("/add")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Cart> addToCart(@RequestParam Long productId,
                                          @RequestParam @Min(value =1,message = "Quantity must be at least 1") int quantity,
                                          @AuthenticationPrincipal UserDetails user) {
        String username = user.getUsername();
        Cart cart = cartService.addToCart(username, productId, quantity);
        return ResponseEntity.ok(cart);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Cart> getCart(@AuthenticationPrincipal UserDetails user) {
        String username = user.getUsername();
        Cart cart = cartService.getCartByUsername(username);
        return ResponseEntity.ok(cart);
    }


    @DeleteMapping("/remove")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Cart> removeItem(@RequestParam Long productId,
                                           @AuthenticationPrincipal UserDetails user) {
        String username = user.getUsername();
        Cart cart = cartService.removeItemByUsername(username, productId);
        return ResponseEntity.ok(cart);
    }



    @PutMapping("/update")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Cart> updateQuantity(@RequestParam Long productId,
                                               @RequestParam @Min(value =1,message = "Quantity must be at least 1")int quantity,
                                               @AuthenticationPrincipal UserDetails user) {
        String username = user.getUsername();
        Cart cart = cartService.updateQuantityByUsername(username, productId, quantity);
        return ResponseEntity.ok(cart);
    }
}