package com.example.puste.repository;

import com.example.puste.model.Product;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple in-memory product store.
 */
@Repository
public class ProductRepository {

    private final Map<Long, Product> store = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    public Product save(Product product) {
        // Java 21: var inference (stable since Java 10), used here for clarity
        var id = (product.id() == null) ? idSequence.getAndIncrement() : product.id();
        var saved = new Product(id, product.name(), product.price(), product.category());
        store.put(id, saved);
        return saved;
    }

    public Optional<Product> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Product> findAll() {
        return new ArrayList<>(store.values());
    }

    public boolean deleteById(long id) {
        return store.remove(id) != null;
    }

    public int count() {
        return store.size();
    }
}
