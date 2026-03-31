package com.mjs.ecommerce.service;

import com.mjs.ecommerce.model.Product;
import com.mjs.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService implements ProductServiceI {

    @Autowired
    ProductRepository pr;

    @Override
    public Product addpr(Product p) {
        return pr.save(p);
    }

    @Override
    public Optional<Product> getpr(long id) {
        return pr.findById(id);
    }

    @Override
    public List<Product> getallpr() {
        return pr.findAll();
    }

    @Override
    public Product update(long id, Product up) {
        Product og=pr.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        og.setName(up.getName());
        og.setDescription(up.getDescription());
        og.setPrice(up.getPrice());
        og.setImageUrl(up.getImageUrl());
        og.setStockQuantity(up.getStockQuantity());
        return pr.save(og);
    }

    @Override
    public void deletepr(long id) {
        Product p=pr.findById(id).orElseThrow(()->new RuntimeException("Product Not found"));
        pr.delete(p);
    }
}
