package com.mjs.ecommerce.service;

import com.mjs.ecommerce.constants.Constants;
import com.mjs.ecommerce.exception.ProductNotFoundException;
import com.mjs.ecommerce.model.Product;
import com.mjs.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImp implements ProductService {

    @Autowired
    ProductRepository pr;

    @Override
    public Product addProduct(Product p) {
        return pr.save(p);
    }

    @Override
    public Optional<Product> getProductById(long id) {
        return pr.findById(id);
    }

    @Override
    public List<Product> getAllProduct() {
        return pr.findAll();
    }

    @Override
    public Product update(long id, Product up) {
        Product og = pr.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));

        applyUpdates(og, up);

        return pr.save(og);
    }

    private void applyUpdates(Product existing, Product updated) {
        Optional.ofNullable(updated.getName())
                .ifPresent(existing::setName);

        Optional.ofNullable(updated.getDescription())
                .ifPresent(existing::setDescription);

        Optional.ofNullable(updated.getImageUrl())
                .ifPresent(existing::setImageUrl);

        if (updated.getPrice() > 0) {
            existing.setPrice(updated.getPrice());
        }

        if (updated.getStockQuantity() >= 0) {
            existing.setStockQuantity(updated.getStockQuantity());
        }
    }
    @Override
    public void deleteProduct(long id) {
        Product p=pr.findById(id).orElseThrow(()->new RuntimeException(Constants.PRODUCT_NOT_FOUND));
        pr.delete(p);
    }
}
