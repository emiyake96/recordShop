package org.yearup.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.yearup.models.Product;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Sql(scripts = "classpath:test-insert-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ProductRepositoryTest
{
    @Autowired
    private ProductRepository productRepository;

    @Test
    public void getById_shouldReturn_theCorrectProduct()
    {
        // arrange
        int productId = 1;

        // act
        Product actual = productRepository.findById(productId).orElse(null);

        // assert
        assertNotNull(actual, "Because product 1 should exist in the test database.");
        assertEquals(499.99, actual.getPrice(), 0.001, "Because I tried to get product 1 from the database.");
    }

    // Bug 1 - product search was filtering to featured-only even with no filter applied
    @Test
    public void findAll_shouldReturn_allProducts_notJustFeatured()
    {
        // arrange - test data has 12 products, only 5 are featured
        // act
        List<Product> allProducts = productRepository.findAll();

        // assert - all 12 should be returned, not just the 5 featured ones
        assertEquals(12, allProducts.size(), "findAll should return all 12 products, not just featured ones.");
    }

    @Test
    public void findByCategoryId_shouldReturn_onlyProductsInThatCategory()
    {
        // arrange - category 1 (Electronics) has 3 products in test data
        // act
        List<Product> electronics = productRepository.findByCategoryId(1);

        // assert
        assertEquals(3, electronics.size(), "Category 1 should have 3 products.");
        assertTrue(electronics.stream().allMatch(p -> p.getCategoryId() == 1),
                "All returned products should belong to category 1.");
    }

    // Bug 2 - stock was not being saved on update
    @Test
    public void save_shouldPersist_updatedStock()
    {
        // arrange
        Product product = productRepository.findById(1).orElseThrow();
        int newStock = 999;

        // act
        product.setStock(newStock);
        productRepository.save(product);

        // assert
        Product updated = productRepository.findById(1).orElseThrow();
        assertEquals(newStock, updated.getStock(), "Stock should be updated to 999 after save.");
    }
}
