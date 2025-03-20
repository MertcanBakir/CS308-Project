package cs308.backhend.controller;

import cs308.backhend.model.Product;
import cs308.backhend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // GET /products - Tüm ürünleri getirir
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }
    // GET /products/{id} - Belirtilen ID'ye sahip ürünü getir
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id); // ✅ Direkt çağır
        return ResponseEntity.ok(product);
    }
}