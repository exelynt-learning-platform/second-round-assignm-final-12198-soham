package com.mjs.ecommerce.service;

import com.mjs.ecommerce.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductServiceI {

  public   Product addProduct(Product p);

   public Optional<Product> getProductById(long id);

   public List<Product> getAllProduct();

   public Product update(long id, Product up);

   public void deleteProduct(long id);
}
