package com.example.puste.controller;

import com.example.puste.model.Category;
import com.example.puste.model.OperationResult;
import com.example.puste.model.Product;
import com.example.puste.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice test for {@link ProductController}.
 */
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean ProductService productService;

    private static final Product SAMPLE = new Product(1L, "Laptop", 1299.00,
            new Category.Electronics("notebook"));

    // ------------------------------------------------------------------
    // GET /api/products
    // ------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/products returns 200 with list")
    void listAll_ok() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(SAMPLE));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Laptop"));
    }

    // ------------------------------------------------------------------
    // GET /api/products/{id}
    // ------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/products/1 returns 200 when product found")
    void getById_found() throws Exception {
        when(productService.getProduct(1L))
                .thenReturn(new OperationResult.Success<>(SAMPLE));

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /api/products/99 returns 404 when not found")
    void getById_notFound() throws Exception {
        when(productService.getProduct(99L))
                .thenReturn(new OperationResult.NotFound<>(99L));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------
    // POST /api/products
    // ------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/products returns 201 on creation")
    void create_success() throws Exception {
        when(productService.createProduct(any()))
                .thenReturn(new OperationResult.Success<>(SAMPLE));

        var body = new ProductController.ProductRequest("Laptop", 1299.00, "electronics", "notebook");

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    @Test
    @DisplayName("POST /api/products returns 400 on validation failure")
    void create_failure() throws Exception {
        when(productService.createProduct(any()))
                .thenReturn(new OperationResult.Failure<>("name must not be blank"));

        var body = new ProductController.ProductRequest("", 10.0, null, null);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------------
    // DELETE /api/products/{id}
    // ------------------------------------------------------------------

    @Test
    @DisplayName("DELETE /api/products/1 returns 204 when deleted")
    void delete_success() throws Exception {
        when(productService.deleteProduct(1L))
                .thenReturn(new OperationResult.Success<>(null));

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/products/99 returns 404 when not found")
    void delete_notFound() throws Exception {
        when(productService.deleteProduct(99L))
                .thenReturn(new OperationResult.NotFound<>(99L));

        mockMvc.perform(delete("/api/products/99"))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------
    // GET /api/products/{id}/summary
    // ------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/products/1/summary returns 200 with summary text")
    void summary_found() throws Exception {
        when(productService.getProduct(1L))
                .thenReturn(new OperationResult.Success<>(SAMPLE));
        when(productService.summarise(any()))
                .thenReturn("summary text");

        mockMvc.perform(get("/api/products/1/summary"))
                .andExpect(status().isOk())
                .andExpect(content().string("summary text"));
    }
}
