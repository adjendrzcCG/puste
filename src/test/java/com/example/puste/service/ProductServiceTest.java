package com.example.puste.service;

import com.example.puste.model.Category;
import com.example.puste.model.OperationResult;
import com.example.puste.model.Product;
import com.example.puste.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ProductService}.
 * Uses AssertJ and JUnit 5 (both bundled with Spring Boot 3 test starter).
 */
class ProductServiceTest {

    private ProductService service;

    @BeforeEach
    void setUp() {
        service = new ProductService(new ProductRepository());
    }

    // ------------------------------------------------------------------
    // Creation
    // ------------------------------------------------------------------

    @Test
    @DisplayName("createProduct returns Success with persisted product")
    void createProduct_success() {
        var product = new Product(null, "Laptop", 999.99,
                new Category.Electronics("notebook"));

        var result = service.createProduct(product);

        assertThat(result).isInstanceOf(OperationResult.Success.class);
        var saved = ((OperationResult.Success<Product>) result).value();
        assertThat(saved.id()).isNotNull();
        assertThat(saved.name()).isEqualTo("Laptop");
    }

    @Test
    @DisplayName("createProduct with blank name returns Failure")
    void createProduct_blankName_failure() {
        var result = service.createProduct(
                new Product(null, "  ", 10.0, new Category.Other("test")));

        assertThat(result).isInstanceOf(OperationResult.Failure.class);
    }

    // ------------------------------------------------------------------
    // Retrieval
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getProduct returns Success for existing id")
    void getProduct_found() {
        var saved = ((OperationResult.Success<Product>)
                service.createProduct(new Product(null, "Widget", 5.0,
                        new Category.Other("misc")))).value();

        var result = service.getProduct(saved.id());

        assertThat(result).isInstanceOf(OperationResult.Success.class);
    }

    @Test
    @DisplayName("getProduct returns NotFound for unknown id")
    void getProduct_notFound() {
        var result = service.getProduct(999L);

        assertThat(result).isInstanceOf(OperationResult.NotFound.class);
        assertThat(((OperationResult.NotFound<Product>) result).id()).isEqualTo(999L);
    }

    // ------------------------------------------------------------------
    // Discount logic
    // ------------------------------------------------------------------

    @Test
    @DisplayName("applyDiscount gives 15 % off for price > 1000")
    void applyDiscount_luxury() {
        var product = new Product(1L, "TV", 2000.0, new Category.Electronics("TV"));

        double discounted = service.applyDiscount(product);

        assertThat(discounted).isEqualTo(2000.0 * 0.85);
    }

    @Test
    @DisplayName("applyDiscount gives 10 % off for 100 < price <= 1000")
    void applyDiscount_midRange() {
        var product = new Product(1L, "Headphones", 250.0,
                new Category.Electronics("audio"));

        double discounted = service.applyDiscount(product);

        assertThat(discounted).isEqualTo(250.0 * 0.90);
    }

    @Test
    @DisplayName("applyDiscount gives 5 % off for price <= 100")
    void applyDiscount_budget() {
        var product = new Product(1L, "Cable", 9.99,
                new Category.Electronics("accessory"));

        double discounted = service.applyDiscount(product);

        assertThat(discounted).isEqualTo(9.99 * 0.95);
    }

    // ------------------------------------------------------------------
    // Summary / sealed-class pattern matching
    // ------------------------------------------------------------------

    @Test
    @DisplayName("summarise produces non-empty string for every category type")
    void summarise_allCategories() {
        var categories = new Category[]{
                new Category.Electronics("laptop"),
                new Category.Clothing("M"),
                new Category.Food(true),
                new Category.Other("gadget")
        };
        for (var cat : categories) {
            var product = new Product(1L, "Item", 50.0, cat);
            assertThat(service.summarise(product)).isNotBlank();
        }
    }

    // ------------------------------------------------------------------
    // Deletion
    // ------------------------------------------------------------------

    @Test
    @DisplayName("deleteProduct returns Success when product exists")
    void deleteProduct_success() {
        var saved = ((OperationResult.Success<Product>)
                service.createProduct(new Product(null, "Mug", 12.0,
                        new Category.Other("kitchenware")))).value();

        var result = service.deleteProduct(saved.id());

        assertThat(result).isInstanceOf(OperationResult.Success.class);
    }

    @Test
    @DisplayName("deleteProduct returns NotFound when product does not exist")
    void deleteProduct_notFound() {
        var result = service.deleteProduct(42L);

        assertThat(result).isInstanceOf(OperationResult.NotFound.class);
    }

    // ------------------------------------------------------------------
    // formatResult
    // ------------------------------------------------------------------

    @Test
    @DisplayName("formatResult renders all three sealed subtypes")
    void formatResult_allVariants() {
        assertThat(service.formatResult(new OperationResult.Success<>("ok")))
                .startsWith("Success:");
        assertThat(service.formatResult(new OperationResult.Failure<>("boom")))
                .startsWith("Error:");
        assertThat(service.formatResult(new OperationResult.NotFound<>(7L)))
                .contains("7");
    }
}
