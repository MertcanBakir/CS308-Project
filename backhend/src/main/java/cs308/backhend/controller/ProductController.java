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

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/popular")
    public List<Product> getPopularProducts() {
        return productService.getMostPopularProducts();
    }

    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String query) {
        return productService.searchProducts(query);
    }

    @GetMapping("/category/{categoryId}")
    public List<Product> getProductsByCategory(@PathVariable Long categoryId) {
        return productService.getProductsByCategoryId(categoryId);
    }
    @PatchMapping("/{id}/update-basic-info")
    public ResponseEntity<Product> updateProductBasicInfo(@PathVariable Long id, @RequestBody Product updatedProduct) {
        Product updated = productService.updateProductNameAndStock(id, updatedProduct.getName(), updatedProduct.getQuantityInStock());
        return ResponseEntity.ok(updated);
    }


}