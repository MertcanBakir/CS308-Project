package cs308.backhend.controller;

import cs308.backhend.model.Category;
import cs308.backhend.model.Product;
import cs308.backhend.model.Role;
import cs308.backhend.model.User;
import cs308.backhend.repository.UserRepo;
import cs308.backhend.security.JwtUtil;
import cs308.backhend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

import java.util.*;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final JwtUtil jwtUtil;
    private final UserRepo userRepo;

    @Autowired
    public ProductController(ProductService productService, JwtUtil jwtUtil, UserRepo userRepo) {
        this.productService = productService;
        this.jwtUtil = jwtUtil;
        this.userRepo = userRepo;
    }


    @GetMapping
    public List<Product> getApprovedProducts() {
        return productService.getApprovedProducts();
    }

    @GetMapping("/all")
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

    @PutMapping("/{id}/update-price")
    public ResponseEntity<String> updateProductPrice(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload,
            @RequestHeader("Authorization") String token) {

        try {
            String jwt = token.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(jwt);

            User user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!jwtUtil.validateToken(jwt, email)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }

            if (user.getRole() != Role.salesManager) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
            }

            Product product = productService.getProductById(id);

            if (payload.get("price") == null) {
                return ResponseEntity.badRequest().body("Price is required.");
            }

            BigDecimal currentPrice = product.getPrice();
            BigDecimal newPrice = new BigDecimal(payload.get("price").toString());
            product.setPrice(newPrice);

            // Eğer daha önce fiyat yoksa ve bu ilk defa fiyat ekleniyorsa
            if (currentPrice == null) {
                product.setApproved(true);
            }

            productService.save(product);

            return ResponseEntity.ok("Product price updated successfully.");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating product price");
        }
    }

    @PatchMapping("/{id}/update-basic-info")
    public ResponseEntity<Product> updateProductBasicInfo(@PathVariable Long id, @RequestBody Product updatedProduct) {
        Product updated = productService.updateProductNameAndStock(id, updatedProduct.getName(), updatedProduct.getQuantityInStock());
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Boolean>> addProduct(
            @RequestBody Map<String, Object> requestBody,
            @RequestHeader("Authorization") String token) {

        Map<String, Boolean> response = new HashMap<>();

        try {
            String jwt = token.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(jwt);

            User user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!jwtUtil.validateToken(jwt, email)) {
                response.put("success", false);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            if (user.getRole() != Role.productManager) {
                response.put("success", false);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            Product product = new Product();
            product.setName((String) requestBody.get("name"));
            product.setSerialNumber((String) requestBody.get("serialNumber"));
            product.setModel((String) requestBody.get("model"));
            product.setDescription((String) requestBody.get("description"));
            product.setDistributorInfo((String) requestBody.get("distributorInfo"));
            product.setImageUrl((String) requestBody.get("imageUrl"));
            product.setWarrantyStatus((Boolean) requestBody.get("warrantyStatus"));
            product.setQuantityInStock((Integer) requestBody.get("quantityInStock"));
            product.setApproved(false);

            List<Integer> categoryIds = (List<Integer>) requestBody.get("categoryIds");
            Set<Category> categorySet = new HashSet<>();
            for (Integer catId : categoryIds) {
                Category category = new Category();
                category.setId(Long.valueOf(catId));
                categorySet.add(category);
            }
            product.setCategories(categorySet);

            productService.addProduct(product);
            response.put("success", true);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

