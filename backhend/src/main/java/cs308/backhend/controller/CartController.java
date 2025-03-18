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
                return ResponseEntity.status(200).body(response);
            }

            List<Product> products = new ArrayList<>();
            for (Wishlist wishlist : wishlistItems) {
                products.add(wishlist.getProduct());
            }

            response.put("success", true);
            List<Map<String, Object>> productList = new ArrayList<>();

            for (Product product : products) {
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

                    Set<Category> categories = new HashSet<>(product.getCategories());
                    List<String> categoryNames = new ArrayList<>();
                    for (Category category : categories) {
                        categoryNames.add(category.getName());
                    }
                    productDetails.put("categories", categoryNames);

                    productList.add(productDetails);
                }
            }

            response.put("products", productList);
            return ResponseEntity.status(200).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }



    @PostMapping("/add_to_cart")
    public ResponseEntity<Map<String,Object>> add_product_to_cart(@RequestBody Map<String, Object> requestBody, @RequestHeader("Authorization") String token) {
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

            long user_id = jwtUtil.getUserIdFromToken(token);
            long product_id = Long.parseLong(requestBody.get("product_id").toString());

            try {
                wishListService.addToWishlist(user_id, product_id);
                response.put("success", true);
                response.put("message", "Product added to wishlist!");
                return ResponseEntity.ok(response);
            } catch (RuntimeException e) {
                response.put("success", false);
                response.put("message", e.getMessage());
                return ResponseEntity.status(400).body(response);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Unexpected error occurred: " + e.getMessage());
            System.out.println(e.getMessage());
            return ResponseEntity.status(500).body(response);
        }

    }

    @DeleteMapping("/delete_from_cart")
    public ResponseEntity<Map<String, Object>> delete_product_from_cart(@RequestBody Map<String, Object> requestBody, @RequestHeader("Authorization") String token) {
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

            long wish_list_id = Long.parseLong(requestBody.get("wish_list_id").toString());

            try{
                wishListService.deleteFromWishlist(wish_list_id);
                response.put("success", true);
                return ResponseEntity.ok(response);
            }
            catch(RuntimeException e){
                response.put("success", false);
                response.put("message", e.getMessage());
                return ResponseEntity.status(400).body(response);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }

    }
}
