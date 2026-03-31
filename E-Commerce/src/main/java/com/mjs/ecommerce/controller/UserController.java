package com.mjs.ecommerce.controller;

import com.mjs.ecommerce.model.User;
import com.mjs.ecommerce.service.UserServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserServiceI ui;


    // CREATE - Public endpoint
    @PostMapping("create")
    public ResponseEntity<User> create(@Valid @RequestBody User user) {
        User saved = ui.createUser(user);
        return ResponseEntity.status(201).body(saved);
    }

    // GET ALL - Admin only (controlled by SecurityConfig)
    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        return ResponseEntity.ok(ui.getAllUsers());
    }

    // GET BY ID - Admin only (controlled by SecurityConfig)
    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ui.getUserById(id));
    }

    // UPDATE - Admin only (controlled by SecurityConfig)
    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Long id,
                                       @RequestBody User user) {
        return ResponseEntity.ok(ui.updateUser(id, user));
    }

    // DELETE - Admin only (controlled by SecurityConfig)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ui.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}