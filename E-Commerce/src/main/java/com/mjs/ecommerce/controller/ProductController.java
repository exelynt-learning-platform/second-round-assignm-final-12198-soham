package com.mjs.ecommerce.controller;

import com.mjs.ecommerce.model.Product;
import com.mjs.ecommerce.service.ProductServiceI;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

        return ResponseEntity.ok().body(ps.addpr(p));
    }

    @GetMapping("/getProduct/{id}")
    public ResponseEntity<Optional<Product>> getProduct(@Valid @PathVariable long id){

return ResponseEntity.ok().body(ps.getpr(id));
    }


    @GetMapping
    public ResponseEntity<List<Product>> getallProduct(){
        return ResponseEntity.ok().body(ps.getallpr());
    }


    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable long id,@Valid @RequestBody Product up){
        return ResponseEntity.ok().body(ps.update(id,up));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable long id){
        ps.deletepr(id);
        return ResponseEntity.ok().body("Deleted Product");
    }
}
