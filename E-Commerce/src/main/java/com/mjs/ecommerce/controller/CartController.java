package com.mjs.ecommerce.controller;

import com.mjs.ecommerce.model.Cart;
import com.mjs.ecommerce.service.CartServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartServiceI cs;


    @PostMapping("/add")
    public ResponseEntity<Cart> addToCart(@RequestParam Long userId,
                                          @RequestParam Long productId,
                                          @RequestParam int quantity) {

        Cart cart = cs.addToCart(userId, productId, quantity);
        return ResponseEntity.ok(cart);
    }


    @GetMapping("/{userId}")
    public ResponseEntity<Cart> getCart(@PathVariable Long userId) {

        Cart cart = cs.getCart(userId);
        return ResponseEntity.ok(cart);
    }


    @DeleteMapping("/remove")
    public ResponseEntity<Cart> removeItem(@RequestParam Long userId,
                                           @RequestParam Long productId) {

        Cart cart = cs.removeItem(userId, productId);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/removeall")
    public ResponseEntity<Void> removeall() {

        cs.removeall();
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/update")
    public ResponseEntity<Cart> updateQuantity(@RequestParam Long userId,
                                               @RequestParam Long productId,
                                               @RequestParam int quantity) {

        Cart cart = cs.updateQuantity(userId, productId, quantity);
        return ResponseEntity.ok(cart);
    }
}