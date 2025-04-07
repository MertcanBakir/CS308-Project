package cs308.backhend.controller;

import cs308.backhend.model.Category;
import cs308.backhend.model.Product;
import cs308.backhend.model.User;
import cs308.backhend.model.Wishlist;
import cs308.backhend.repository.ProductRepo;
import cs308.backhend.repository.UserRepo;
import cs308.backhend.repository.WishListRepo;
import cs308.backhend.security.JwtUtil;
import cs308.backhend.service.WishListService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class CartController {

    private final JwtUtil jwtUtil;
    private final UserRepo userRepo;
    private final WishListService wishListService;
    private final WishListRepo wishListRepo;
    private final ProductRepo productRepo;

    @Autowired
    public CartController(UserRepo userRepo, WishListRepo wishListRepo, ProductRepo productRepo, JwtUtil jwtUtil,WishListService wishListService) {
        this.userRepo = userRepo;
        this.wishListRepo = wishListRepo;
        this.productRepo = productRepo;
        this.jwtUtil = jwtUtil;
        this.wishListService = wishListService;
    }

    @GetMapping("/cart")
    public ResponseEntity<Map<String, Object>> getWishlistProducts(@RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            String userEmail = jwtUtil.extractEmail(token);
            User user = userRepo.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!jwtUtil.validateToken(token, userEmail)) {
                response.put("success", false);
                response.put("message", "Invalid or expired token!");
                return ResponseEntity.status(401).body(response);
            }

            List<Wishlist> wishlistItems = wishListRepo.findByUser(user);

            if (wishlistItems.isEmpty()) {
                response.put("success", true);
                response.put("message", "Wishlist is empty.");
                response.put("products", new ArrayList<>());
                return ResponseEntity.status(200).body(response);
            }

            List<Map<String, Object>> productList = new ArrayList<>();

            for (Wishlist wishlist : wishlistItems) {
                Product product = wishlist.getProduct();
                if (product != null) {
                    Map<String, Object> productDetails = new HashMap<>();
                    productDetails.put("id", product.getId());
                    productDetails.put("name", product.getName());
                    productDetails.put("model", product.getModel());
                    productDetails.put("serialNumber", product.getSerialNumber());
                    productDetails.put("description", product.getDescription());
                    productDetails.put("quantityInStock", product.getQuantityInStock());
                    productDetails.put("price", product.getPrice());
                    productDetails.put("warrantyStatus", product.isWarrantyStatus());
                    productDetails.put("distributorInfo", product.getDistributorInfo());
                    productDetails.put("imageUrl", product.getImageUrl());

                    productDetails.put("quantity", wishlist.getQuantity());
                    productDetails.put("wishlistId", wishlist.getId());

                    productList.add(productDetails);
                }
            }

            response.put("success", true);
            response.put("products", productList);
            return ResponseEntity.status(200).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PutMapping("/change_quantity")
    public ResponseEntity<Map<String,Object>> ChangeTheQuantity( @RequestBody Map<String, Object> requestBody,@RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();

        String userEmail = jwtUtil.extractEmail(token);
        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!jwtUtil.validateToken(token, userEmail)) {
            response.put("success", false);
            response.put("message", "Invalid or expired token!");
            return ResponseEntity.status(401).body(response);
        }

        long productId = Long.parseLong(requestBody.get("product_id").toString());
        long quantityToAdd = Long.parseLong(requestBody.get("quantity").toString());

        if (quantityToAdd <= 0) {
            response.put("success", false);
            response.put("message", "Invalid quantity!");
            return ResponseEntity.status(400).body(response);
        }

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Optional<Wishlist> wishlistItemOpt = wishListRepo.findByUserAndProduct(user, product);

        if (wishlistItemOpt.isPresent()) {
            Wishlist wishlistItem = wishlistItemOpt.get();
            wishlistItem.setQuantity(quantityToAdd);
            wishListRepo.save(wishlistItem);
            response.put("message", "Product quantity updated in wishlist!");
            return ResponseEntity.status(200).body(response);
        }

        response.put("success", false);
        response.put("message", "Hata");
        return ResponseEntity.status(404).body(response);
    }

    @Transactional
    @PostMapping("/add_to_cart")
    public ResponseEntity<Map<String, Object>> addProductToCart(
            @RequestBody Map<String, Object> requestBody,
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();

        try {
            String userEmail = jwtUtil.extractEmail(token);
            User user = userRepo.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!jwtUtil.validateToken(token, userEmail)) {
                response.put("success", false);
                response.put("message", "Invalid or expired token!");
                return ResponseEntity.status(401).body(response);
            }

            long productId = Long.parseLong(requestBody.get("product_id").toString());
            long quantityToAdd = Long.parseLong(requestBody.get("quantity").toString());

            if (quantityToAdd <= 0) {
                response.put("success", false);
                response.put("message", "Invalid quantity!");
                return ResponseEntity.status(400).body(response);
            }

            Product product = productRepo.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            Optional<Wishlist> wishlistItemOpt = wishListRepo.findByUserAndProduct(user, product);

            if (wishlistItemOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Ürün zaten sepette!");
                return ResponseEntity.status(400).body(response);
            }

            Wishlist newWishlistItem = new Wishlist();
            newWishlistItem.setUser(user);
            newWishlistItem.setProduct(product);
            newWishlistItem.setQuantity(quantityToAdd);
            wishListRepo.save(newWishlistItem);

            product.incrementWishlistCount();
            productRepo.save(product);

            response.put("success", true);
            response.put("message", "Product added to wishlist!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/delete_from_cart")
    public ResponseEntity<Map<String, Object>> delete_product_from_cart(
            @RequestBody Map<String, Object> requestBody,
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();

        try {

            String userEmail = jwtUtil.extractEmail(token);
            Optional<User> userOptional = userRepo.findByEmail(userEmail);
            if (userOptional.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found!");
                return ResponseEntity.status(401).body(response);
            }

            if (!jwtUtil.validateToken(token, userEmail)) {
                response.put("success", false);
                response.put("message", "Invalid or expired token!");
                return ResponseEntity.status(401).body(response);
            }

            long product_id = Long.parseLong(requestBody.get("product_id").toString());

            Product product = productRepo.findById(product_id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            Optional<Wishlist> wishlistItem = wishListRepo.findByUserAndProduct(userOptional.get(), product);

            if (wishlistItem.isEmpty()) {
                response.put("success", false);
                response.put("message", "Wishlist item not found");
                return ResponseEntity.status(400).body(response);
            }
            
            product.decrementWishlistCount();
            productRepo.save(product);

            wishListRepo.delete(wishlistItem.get());
            response.put("success", true);
            response.put("message", "Product removed from wishlist");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}