package com.example.puste.model;

/**
 * Sealed interface hierarchy for product categories.
 * Java 21 feature: Sealed classes/interfaces (stable since Java 17).
 *
 * <p>Only the listed permits are valid implementations, enabling exhaustive
 * pattern matching in switch expressions without a default branch.</p>
 */
public sealed interface Category
        permits Category.Electronics, Category.Clothing, Category.Food, Category.Other {

    /** Human-readable label for the category. */
    String label();

    record Electronics(String subType) implements Category {
        @Override
        public String label() {
            return "Electronics / " + subType;
        }
    }

    record Clothing(String size) implements Category {
        @Override
        public String label() {
            return "Clothing (size: " + size + ")";
        }
    }

    record Food(boolean isOrganic) implements Category {
        @Override
        public String label() {
            return isOrganic ? "Food (organic)" : "Food (conventional)";
        }
    }

    record Other(String description) implements Category {
        @Override
        public String label() {
            return "Other: " + description;
        }
    }
}
