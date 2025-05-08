package cs308.backhend.controller;

import cs308.backhend.model.Category;
import cs308.backhend.model.Product;
import cs308.backhend.model.Role;
import cs308.backhend.model.User;
import cs308.backhend.repository.UserRepo;
import cs308.backhend.security.JwtUtil;
import cs308.backhend.service.ProductService;
import cs308.backhend.service.WishlistNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final JwtUtil jwtUtil;
    private final UserRepo userRepo;
    private final WishlistNotificationService wishlistNotificationService;

    @Autowired
    public ProductController(ProductService productService, JwtUtil jwtUtil, UserRepo userRepo,WishlistNotificationService wishlistNotificationService) {
        this.productService = productService;
        this.jwtUtil = jwtUtil;
        this.userRepo = userRepo;
        this.wishlistNotificationService = wishlistNotificationService;
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

            // Don't proceed if the price hasn't actually changed
            if (currentPrice != null && currentPrice.compareTo(newPrice) == 0) {
                return ResponseEntity.ok("Price unchanged - no update needed.");
            }

            // Store the old price before updating
            BigDecimal oldPrice = currentPrice != null ? currentPrice : BigDecimal.ZERO;

            // Update the product price
            product.setPrice(newPrice);

            // If this is the first time setting a price, mark as approved
            if (currentPrice == null) {
                product.setApproved(true);
            }

            // Save the updated product
            productService.save(product);

            // Notify users who have this product in their wishlist
            // This is done asynchronously to not block the response
            CompletableFuture.runAsync(() -> {
                try {
                    wishlistNotificationService.notifyUsersOfPriceChange(product, oldPrice, newPrice);
                } catch (Exception e) {
                    // Log the error but don't affect the API response
                    e.printStackTrace();
                }
            });

            return ResponseEntity.ok("Product price updated successfully. Wishlist notifications have been scheduled.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating product price: " + e.getMessage());
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

