package com.mjs.ecommerce.repository;

import com.mjs.ecommerce.model.CartItem;
import com.mjs.ecommerce.model.Cart;
import com.mjs.ecommerce.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class CartItemRepositoryTest {

    @Autowired
    private CartItemRepository repository;

    @Test
    void findById_notFound() {

        Optional<CartItem> result = repository.findById(999L);

        assertTrue(result.isEmpty());
    }



}