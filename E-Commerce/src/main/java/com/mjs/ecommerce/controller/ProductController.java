package com.mjs.ecommerce.controller;

import com.mjs.ecommerce.model.Product;
import com.mjs.ecommerce.service.ProductServiceI;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    ProductServiceI ps;

    @PostMapping
    public ResponseEntity<Product> addProduct(@Valid @RequestBody Product p){

        return ResponseEntity.ok().body(ps.addProduct(p));
    }

    @GetMapping("/getProduct/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Optional<Product>> getProduct(@Valid @PathVariable long id){
    return ResponseEntity.ok().body(ps.getProductById(id));
    }


    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<Product>> getallProduct(){
        return ResponseEntity.ok().body(ps.getAllProduct());
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> update(@PathVariable long id,@Valid @RequestBody Product up){
        return ResponseEntity.ok().body(ps.update(id,up));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteProduct(@PathVariable long id){
        ps.deleteProduct(id);
        return ResponseEntity.ok().body("Deleted Product");
    }
}
