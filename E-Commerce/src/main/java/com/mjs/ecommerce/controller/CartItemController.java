package com.mjs.ecommerce.controller;

import com.mjs.ecommerce.model.CartItem;
import com.mjs.ecommerce.service.CartItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart-items")
public class CartItemController {

    @Autowired
    private CartItemService cis;

    @PostMapping
    public ResponseEntity<CartItem> create(@Valid @RequestBody CartItem item) {
        return ResponseEntity.status(201).body(cis.createCartItem(item));
    }

    @GetMapping
    public ResponseEntity<List<CartItem>> getAll() {
        return ResponseEntity.ok(cis.getAllCartItems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartItem> getById(@PathVariable Long id) {
        return ResponseEntity.ok(cis.getCartItemById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cis.deleteCartItem(id);
        return ResponseEntity.noContent().build();
    }
}