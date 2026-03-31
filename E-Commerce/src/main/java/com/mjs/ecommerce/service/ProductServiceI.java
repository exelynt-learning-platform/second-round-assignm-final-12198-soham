package com.mjs.ecommerce.service;

import com.mjs.ecommerce.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductServiceI {

  public   Product addpr(Product p);

   public Optional<Product> getpr(long id);

   public List<Product> getallpr();

   public Product update(long id, Product up);

   public void deletepr(long id);
}
