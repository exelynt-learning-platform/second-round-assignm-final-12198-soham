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
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    ProductServiceI ps;

    @PostMapping
    public ResponseEntity<Product> addpr(@Valid @RequestBody Product p){

        return ResponseEntity.ok().body(ps.addpr(p));
    }

    @GetMapping("/getProduct/{id}")
    public ResponseEntity<Optional<Product>> getpr(@Valid @PathVariable long id){

return ResponseEntity.ok().body(ps.getpr(id));
    }


    @GetMapping("/getProductList")
    public ResponseEntity<List<Product>> getallpr(){
        return ResponseEntity.ok().body(ps.getallpr());
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<Product> update(@PathVariable long id,@Valid @RequestBody Product up){
        return ResponseEntity.ok().body(ps.update(id,up));
    }
    
    @DeleteMapping("/deleteProduct/{id}")
    public ResponseEntity<String> delete(@PathVariable long id){
        ps.deletepr(id);
        return ResponseEntity.ok().body("Deleted Product");
    }
}
