package com.example.puste.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Immutable product record — replaces a traditional Java Bean / POJO.
 * Java 21 feature: Records (finalized in Java 16, fully stable in 21).
 *
 * @param id       unique product identifier
 * @param name     product name
 * @param price    product price (must be positive)
 * @param category product category
 */
public record Product(
        Long id,
        @NotBlank String name,
        @Positive double price,
        Category category
) {
    /**
     * Convenience factory using a text block for the display string.
     */
    public String toDisplayString() {
        // Java 21: Text blocks (stable since Java 15)
        return """
                Product {
                  id       = %d
                  name     = %s
                  price    = %.2f
                  category = %s
                }""".formatted(id, name, price, category);
    }
}
