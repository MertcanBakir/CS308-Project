package cs308.backhend.controller;

import cs308.backhend.model.*;
import cs308.backhend.repository.*;
import cs308.backhend.security.JwtUtil;
import cs308.backhend.service.OrderService;
import cs308.backhend.service.ProductService;
import cs308.backhend.service.WishListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class OrderController {

    private final OrderService orderService;
    private final OrderRepo orderRepo;
    private final UserRepo userRepo;
    private final ProductRepo productRepo;
    private final ProductService productService;
    private final CardRepo cardRepo;
    private final AddressRepo addressRepo;
    private final WishListService wishListService;
    private final JwtUtil jwtUtil;

    public OrderController(OrderService orderService, OrderRepo orderRepo, UserRepo userRepo, ProductRepo productRepo,
                           ProductService productService, CardRepo cardRepo,
                           AddressRepo addressRepo, WishListService wishListService,
                           JwtUtil jwtUtil) {
        this.orderService = orderService;
        this.orderRepo = orderRepo;
        this.userRepo = userRepo;
        this.productRepo = productRepo;
        this.productService = productService;
        this.cardRepo = cardRepo;
        this.addressRepo = addressRepo;
        this.wishListService = wishListService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/add-order")
    public ResponseEntity<Map<String, Object>> addOrder(@RequestBody Map<String, Object> requestBody, @RequestHeader("Authorization") String token) {
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

            Long wishlistId = Long.parseLong(requestBody.get("wishlist_id").toString());
            Long cardId = Long.parseLong(requestBody.get("card_id").toString());
            Long addressId = Long.parseLong(requestBody.get("address_id").toString());

            Wishlist wishlist = wishListService.getWishlistById(wishlistId);
            Product product = wishlist.getProduct();
            int quantity = wishlist.getQuantity().intValue();

            Card card = cardRepo.findById(cardId)
                    .orElseThrow(() -> new RuntimeException("Card not found"));
            Address address = addressRepo.findById(addressId)
                    .orElseThrow(() -> new RuntimeException("Address not found"));

            Order order = new Order();
            order.setUser(user);
            order.setProduct(product);
            order.setCard(card);
            order.setAddress(address);
            order.setQuantity(quantity);
            order.setStatus(OrderStatus.PROCESSING);

            orderRepo.save(order);
            productService.decrementStock(product, quantity);

            wishListService.removeFromWishlist(user.getId(), product.getId());

            orderService.generateInvoiceAndSendEmail(order, user);

            response.put("success", true);
            response.put("message", "The order was created successfully and the invoice was sent via email.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}