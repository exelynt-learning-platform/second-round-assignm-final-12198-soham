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

    // CREATE
    @PostMapping
    public ResponseEntity<CartItem> create(@Valid @RequestBody CartItem item) {
        return ResponseEntity.status(201).body(cis.createCartItem(item));
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<CartItem>> getAll() {
        return ResponseEntity.ok(cis.getAllCartItems());
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<CartItem> getById(@PathVariable Long id) {
        return ResponseEntity.ok(cis.getCartItemById(id));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cis.deleteCartItem(id);
        return ResponseEntity.noContent().build();
    }
}