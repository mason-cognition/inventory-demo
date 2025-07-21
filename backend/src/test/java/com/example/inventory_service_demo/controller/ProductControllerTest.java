package com.example.inventory_service_demo.controller;

import com.example.inventory_service_demo.model.Product;
import com.example.inventory_service_demo.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private Product validProduct;
    private Product anotherProduct;

    @BeforeEach
    void setUp() {
        validProduct = new Product();
        validProduct.setId(1L);
        validProduct.setName("Test Product");
        validProduct.setDescription("Test Description");
        validProduct.setSku("TEST-001");
        validProduct.setPrice(new BigDecimal("29.99"));

        anotherProduct = new Product();
        anotherProduct.setId(2L);
        anotherProduct.setName("Another Product");
        anotherProduct.setDescription("Another Description");
        anotherProduct.setSku("TEST-002");
        anotherProduct.setPrice(new BigDecimal("49.99"));
    }

    @Test
    void getAllProducts_ShouldReturnProductList() throws Exception {
        List<Product> products = Arrays.asList(validProduct, anotherProduct);
        when(productService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Product"))
                .andExpect(jsonPath("$[0].sku").value("TEST-001"))
                .andExpect(jsonPath("$[0].price").value(29.99))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Another Product"));
    }

    @Test
    void getProductById_WithValidId_ShouldReturnProduct() throws Exception {
        when(productService.getProductById(1L)).thenReturn(Optional.of(validProduct));

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.sku").value("TEST-001"))
                .andExpect(jsonPath("$.price").value(29.99));
    }

    @Test
    void getProductById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        when(productService.getProductById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProductBySku_WithValidSku_ShouldReturnProduct() throws Exception {
        when(productService.getProductBySku("TEST-001")).thenReturn(Optional.of(validProduct));

        mockMvc.perform(get("/api/products/sku/TEST-001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.sku").value("TEST-001"))
                .andExpect(jsonPath("$.price").value(29.99));
    }

    @Test
    void getProductBySku_WithNonExistentSku_ShouldReturnNotFound() throws Exception {
        when(productService.getProductBySku("NON-EXISTENT")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/products/sku/NON-EXISTENT"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createProduct_WithValidData_ShouldReturnCreatedProduct() throws Exception {
        Product newProduct = new Product();
        newProduct.setName("New Product");
        newProduct.setDescription("New Description");
        newProduct.setSku("NEW-001");
        newProduct.setPrice(new BigDecimal("19.99"));

        Product createdProduct = new Product();
        createdProduct.setId(3L);
        createdProduct.setName("New Product");
        createdProduct.setDescription("New Description");
        createdProduct.setSku("NEW-001");
        createdProduct.setPrice(new BigDecimal("19.99"));

        when(productService.createProduct(any(Product.class))).thenReturn(createdProduct);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("New Product"))
                .andExpect(jsonPath("$.sku").value("NEW-001"))
                .andExpect(jsonPath("$.price").value(19.99));
    }

    @Test
    void createProduct_WithBlankName_ShouldReturnBadRequest() throws Exception {
        Product invalidProduct = new Product();
        invalidProduct.setName("");
        invalidProduct.setDescription("Description");
        invalidProduct.setSku("INVALID-001");
        invalidProduct.setPrice(new BigDecimal("19.99"));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProduct_WithBlankSku_ShouldReturnBadRequest() throws Exception {
        Product invalidProduct = new Product();
        invalidProduct.setName("Valid Name");
        invalidProduct.setDescription("Description");
        invalidProduct.setSku("");
        invalidProduct.setPrice(new BigDecimal("19.99"));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProduct_WithNullPrice_ShouldReturnBadRequest() throws Exception {
        Product invalidProduct = new Product();
        invalidProduct.setName("Valid Name");
        invalidProduct.setDescription("Description");
        invalidProduct.setSku("VALID-001");
        invalidProduct.setPrice(null);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProduct_WithNegativePrice_ShouldReturnBadRequest() throws Exception {
        Product invalidProduct = new Product();
        invalidProduct.setName("Valid Name");
        invalidProduct.setDescription("Description");
        invalidProduct.setSku("VALID-001");
        invalidProduct.setPrice(new BigDecimal("-10.00"));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProduct_WithDuplicateSku_ShouldReturnBadRequest() throws Exception {
        Product duplicateSkuProduct = new Product();
        duplicateSkuProduct.setName("Duplicate SKU Product");
        duplicateSkuProduct.setDescription("Description");
        duplicateSkuProduct.setSku("DUPLICATE-001");
        duplicateSkuProduct.setPrice(new BigDecimal("19.99"));

        when(productService.createProduct(any(Product.class)))
                .thenThrow(new IllegalArgumentException("Product with SKU DUPLICATE-001 already exists"));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateSkuProduct)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProduct_WithValidData_ShouldReturnUpdatedProduct() throws Exception {
        Product updateData = new Product();
        updateData.setName("Updated Product");
        updateData.setDescription("Updated Description");
        updateData.setSku("UPDATED-001");
        updateData.setPrice(new BigDecimal("39.99"));

        Product updatedProduct = new Product();
        updatedProduct.setId(1L);
        updatedProduct.setName("Updated Product");
        updatedProduct.setDescription("Updated Description");
        updatedProduct.setSku("UPDATED-001");
        updatedProduct.setPrice(new BigDecimal("39.99"));

        when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(updatedProduct);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Product"))
                .andExpect(jsonPath("$.sku").value("UPDATED-001"))
                .andExpect(jsonPath("$.price").value(39.99));
    }

    @Test
    void updateProduct_WithBlankName_ShouldReturnBadRequest() throws Exception {
        Product invalidUpdateData = new Product();
        invalidUpdateData.setName("");
        invalidUpdateData.setDescription("Description");
        invalidUpdateData.setSku("VALID-001");
        invalidUpdateData.setPrice(new BigDecimal("19.99"));

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdateData)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProduct_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        Product updateData = new Product();
        updateData.setName("Valid Name");
        updateData.setDescription("Description");
        updateData.setSku("VALID-001");
        updateData.setPrice(new BigDecimal("19.99"));

        when(productService.updateProduct(eq(999L), any(Product.class)))
                .thenThrow(new IllegalArgumentException("Product not found with id: 999"));

        mockMvc.perform(put("/api/products/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProduct_WithDuplicateSku_ShouldReturnNotFound() throws Exception {
        Product updateData = new Product();
        updateData.setName("Valid Name");
        updateData.setDescription("Description");
        updateData.setSku("DUPLICATE-SKU");
        updateData.setPrice(new BigDecimal("19.99"));

        when(productService.updateProduct(eq(1L), any(Product.class)))
                .thenThrow(new IllegalArgumentException("Product with SKU DUPLICATE-SKU already exists"));

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct_WithValidId_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProduct_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        doThrow(new IllegalArgumentException("Product not found with id: 999"))
                .when(productService).deleteProduct(999L);

        mockMvc.perform(delete("/api/products/999"))
                .andExpect(status().isNotFound());
    }
}
