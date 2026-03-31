package com.mjs.ecommerce.service;

import com.mjs.ecommerce.Constants;
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

        Product og = pr.findById(id)
                .orElseThrow(() -> new RuntimeException(Constants.PRODUCT_NOT_FOUND));

        if (up.getName() != null) {
            og.setName(up.getName());
        }

        if (up.getDescription() != null) {
            og.setDescription(up.getDescription());
        }

        if (up.getPrice() != 0) { // ⚠️ assuming 0 is invalid
            og.setPrice(up.getPrice());
        }

        if (up.getImageUrl() != null) {
            og.setImageUrl(up.getImageUrl());
        }

        og.setStockQuantity(up.getStockQuantity());

        return pr.save(og);
    }

    @Override
    public void deletepr(long id) {
        Product p=pr.findById(id).orElseThrow(()->new RuntimeException(Constants.PRODUCT_NOT_FOUND));
        pr.delete(p);
    }
}
