package com.mjs.ecommerce.controller;

import com.mjs.ecommerce.model.CartItem;
import com.mjs.ecommerce.service.CartItemServiceI;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart-items")
public class CartItemController {

    @Autowired
    private CartItemServiceI cis;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CartItem> create(@Valid @RequestBody CartItem item) {
        return ResponseEntity.status(201).body(cis.createCartItem(item));
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<CartItem>> getAll() {
        return ResponseEntity.ok(cis.getAllCartItems());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CartItem> getById(@PathVariable Long id) {
        return ResponseEntity.ok(cis.getCartItemById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cis.deleteCartItem(id);
        return ResponseEntity.noContent().build();
    }
}