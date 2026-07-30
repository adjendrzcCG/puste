package com.example.puste.service;

import com.example.puste.model.Category;
import com.example.puste.model.OperationResult;
import com.example.puste.model.Product;
import com.example.puste.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Business logic for product management.
 *
 * <p>Demonstrates several Java 21 language features:</p>
 * <ul>
 *   <li>Pattern matching for {@code switch} (JEP 441, stable Java 21)</li>
 *   <li>Sealed-class exhaustive switch (no default needed)</li>
 *   <li>Records and deconstruction patterns</li>
 *   <li>Text blocks</li>
 * </ul>
 */
@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public OperationResult<Product> createProduct(Product product) {
        // Validate at the service boundary — keeps the record a plain data holder
        if (product.name() == null || product.name().isBlank()) {
            return new OperationResult.Failure<>("Product name must not be blank");
        }
        if (product.price() <= 0) {
            return new OperationResult.Failure<>("Product price must be positive");
        }
        var saved = repository.save(product);
        return new OperationResult.Success<>(saved);
    }

    public OperationResult<Product> getProduct(long id) {
        return repository.findById(id)
                .<OperationResult<Product>>map(OperationResult.Success::new)
                .orElse(new OperationResult.NotFound<>(id));
    }

    public List<Product> getAllProducts() {
        return repository.findAll();
    }

    public OperationResult<Void> deleteProduct(long id) {
        if (repository.deleteById(id)) {
            return new OperationResult.Success<>(null);
        }
        return new OperationResult.NotFound<>(id);
    }

    /**
     * Returns a human-readable summary of a product using
     * Java 21 pattern matching in switch expressions (JEP 441).
     */
    public String summarise(Product product) {
        // Java 21: pattern matching for switch — exhaustive over sealed Category
        String categoryDetail = switch (product.category()) {
            case Category.Electronics(var subType) -> "Electronics subtype: " + subType;
            case Category.Clothing(var size)       -> "Clothing, size " + size;
            case Category.Food(var organic)        -> organic ? "Organic food" : "Conventional food";
            case Category.Other(var desc)          -> "Other: " + desc;
        };

        // Java 21: text blocks with formatted()
        return """
                === Product Summary ===
                Name    : %s
                Price   : $%.2f
                Category: %s
                """.formatted(product.name(), product.price(), categoryDetail);
    }

    /**
     * Determines a discount tier using a Java 21 switch expression
     * (JEP 441 guarded patterns).
     */
    public double applyDiscount(Product product) {
        return switch (product) {
            case Product p when p.price() > 1000 -> p.price() * 0.85;   // 15 % off luxury items
            case Product p when p.price() > 100  -> p.price() * 0.90;   // 10 % off mid-range
            case Product p                       -> p.price() * 0.95;   //  5 % off everything else
        };
    }

    /**
     * Formats an {@link OperationResult} to a response string.
     * Shows exhaustive pattern matching over a sealed interface — no default needed.
     */
    public <T> String formatResult(OperationResult<T> result) {
        return switch (result) {
            case OperationResult.Success<T> s   -> "Success: " + s.value();
            case OperationResult.Failure<T> f   -> "Error: " + f.reason();
            case OperationResult.NotFound<T> nf -> "Not found (id=" + nf.id() + ")";
        };
    }
}
