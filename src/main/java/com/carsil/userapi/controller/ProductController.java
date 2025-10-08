package com.carsil.userapi.controller;

import com.carsil.userapi.model.Product;
import com.carsil.userapi.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public List<Product> getProducts() {
        return productService.getAll();
    }

    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        return productService.create(product);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productService.delete(id);
    }

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return productService.update(product, id);
    }

    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam("q") String q) {
        return productService.search(q);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-op/{op}")
    public ResponseEntity<List<Product>> getProductByOp(@PathVariable String op) {
        List<Product> products = productService.getProductsByOp(op);
        if (products.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(products);
    }

    @GetMapping("/by-team/{teamId}")
    public ResponseEntity<List<Product>> getProductsByTeam(@PathVariable Long teamId) {
        List<Product> products = productService.getProductsByTeam(teamId);
        if (products.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(products);
    }

    @GetMapping("/by-date-range")
    public ResponseEntity<List<Product>> getProductsByDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Product> products = productService.getProductsByDateRange(startDate, endDate);
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{id}/made")
    public Product setMade(@PathVariable Long id, @RequestParam("value") int value) {
        return productService.setMade(id, value);
    }

    @PatchMapping("/{id}/progress")
    public Product incrementProgress(@PathVariable Long id, @RequestParam int delta) {
        return productService.incrementMade(id, delta);
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Product> patchProduct(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates
    ) {
        Product updated = productService.partialUpdate(id, updates);
        return ResponseEntity.ok(updated);
    }
}
