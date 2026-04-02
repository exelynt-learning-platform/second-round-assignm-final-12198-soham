package com.mjs.ecommerce.controller;

import com.mjs.ecommerce.model.Product;
import com.mjs.ecommerce.service.ProductService;
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
    ProductService productService;

    @PostMapping
    public ResponseEntity<Product> addProduct(@Valid @RequestBody Product product){

        return ResponseEntity.ok().body(productService.addProduct(product));
    }

    @GetMapping("{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Optional<Product>> getProduct(@Valid @PathVariable long id){
    return ResponseEntity.ok().body(productService.getProductById(id));
    }


    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<Product>> getallProduct(){
        return ResponseEntity.ok().body(productService.getAllProduct());
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> update(@PathVariable long id,@Valid @RequestBody Product product){
        return ResponseEntity.ok().body(productService.update(id,product));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteProduct(@PathVariable long id){
        productService.deleteProduct(id);
        return ResponseEntity.ok().body("Deleted Product");
    }
}
