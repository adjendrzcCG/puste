package com.example.puste.controller;

import com.example.puste.model.Category;
import com.example.puste.model.OperationResult;
import com.example.puste.model.Product;
import com.example.puste.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for the product resource.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> listAll() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable long id) {
        return switch (productService.getProduct(id)) {
            case OperationResult.Success<Product> s   -> ResponseEntity.ok(s.value());
            case OperationResult.Failure<Product> f   -> ResponseEntity.badRequest().body(f.reason());
            case OperationResult.NotFound<Product> nf ->
                    ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Product %d not found".formatted(nf.id()));
        };
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProductRequest request) {
        var category = resolveCategory(request.categoryType(), request.categoryDetail());
        var product = new Product(null, request.name(), request.price(), category);
        return switch (productService.createProduct(product)) {
            case OperationResult.Success<Product> s   ->
                    ResponseEntity.status(HttpStatus.CREATED).body(s.value());
            case OperationResult.Failure<Product> f   ->
                    ResponseEntity.badRequest().body(f.reason());
            case OperationResult.NotFound<Product> nf ->
                    ResponseEntity.status(HttpStatus.NOT_FOUND).body("id=" + nf.id());
        };
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable long id) {
        return switch (productService.deleteProduct(id)) {
            case OperationResult.Success<Void> ignored  -> ResponseEntity.noContent().build();
            case OperationResult.Failure<Void> f        -> ResponseEntity.badRequest().body(f.reason());
            case OperationResult.NotFound<Void> nf      ->
                    ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Product %d not found".formatted(nf.id()));
        };
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<String> summary(@PathVariable long id) {
        return switch (productService.getProduct(id)) {
            case OperationResult.Success<Product> s ->
                    ResponseEntity.ok(productService.summarise(s.value()));
            case OperationResult.Failure<Product> f ->
                    ResponseEntity.badRequest().body(f.reason());
            case OperationResult.NotFound<Product> nf ->
                    ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Product %d not found".formatted(nf.id()));
        };
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Request DTO — another Java 21 record.
     */
    public record ProductRequest(String name, double price, String categoryType, String categoryDetail) {}

    private Category resolveCategory(String type, String detail) {
        if (type == null) return new Category.Other("unspecified");
        return switch (type.toLowerCase()) {
            case "electronics" -> new Category.Electronics(detail != null ? detail : "general");
            case "clothing"    -> new Category.Clothing(detail != null ? detail : "one-size");
            case "food"        -> new Category.Food("organic".equalsIgnoreCase(detail));
            default            -> new Category.Other(detail != null ? detail : type);
        };
    }
}
