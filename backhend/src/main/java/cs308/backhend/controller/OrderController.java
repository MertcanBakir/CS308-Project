package cs308.backhend.controller;

import cs308.backhend.model.*;
import cs308.backhend.repository.*;
import cs308.backhend.security.JwtUtil;
import cs308.backhend.service.OrderService;
import cs308.backhend.service.ProductService;
import cs308.backhend.service.WishListService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;


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

    @Autowired
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
    @Transactional
    @PostMapping("/add-order")
    public ResponseEntity<Map<String, Object>> addOrder(@RequestBody Map<String, Object> requestBody,
                                                        @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            String userEmail = jwtUtil.extractEmail(token.replace("Bearer ", ""));
            User user = userRepo.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!jwtUtil.validateToken(token.replace("Bearer ", ""), userEmail)) {
                response.put("success", false);
                response.put("message", "Invalid or expired token!");
                return ResponseEntity.status(401).body(response);
            }

            List<Integer> wishlistIds = (List<Integer>) requestBody.get("wishlist_ids");
            Long cardId = Long.parseLong(requestBody.get("card_id").toString());
            Long addressId = Long.parseLong(requestBody.get("address_id").toString());

            Card card = cardRepo.findById(cardId)
                    .orElseThrow(() -> new RuntimeException("Card not found"));
            Address address = addressRepo.findById(addressId)
                    .orElseThrow(() -> new RuntimeException("Address not found"));

            List<Order> orderList = new ArrayList<>();

            for (Integer wishlistId : wishlistIds) {
                Wishlist wishlist = wishListService.getWishlistById(Long.valueOf(wishlistId));
                Product product = wishlist.getProduct();
                int quantity = wishlist.getQuantity().intValue();

                Order order = new Order();
                order.setUser(user);
                order.setProduct(product);
                order.setCard(card);
                order.setAddress(address);
                order.setQuantity(quantity);
                order.setStatus(OrderStatus.PROCESSING);

                orderList.add(order);
                productService.decrementStock(product, quantity);
                wishListService.removeFromWishlist(user.getId(), product.getId());
            }

            orderService.generateInvoiceAndSendEmail(orderList, user);


            for (Order order : orderList) {
                orderRepo.save(order);
            }

            response.put("success", true);
            response.put("message", "All orders placed and invoice sent successfully.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    @GetMapping("/all-order")
    public ResponseEntity<?> getAllOrders(@RequestHeader("Authorization") String token) {
        try {
            String userEmail = jwtUtil.extractEmail(token.replace("Bearer ", ""));
            User user = userRepo.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!jwtUtil.validateToken(token.replace("Bearer ", ""), userEmail)) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Invalid token"));
            }

            if (user.getRole() != Role.productManager) {
                return ResponseEntity.status(403).body(Map.of("success", false, "message", "Unauthorized access"));
            }

            List<Order> orders = orderRepo.findAll(); // Bütün siparişleri getirir

            List<Map<String, Object>> responseOrders = orders.stream().map(order -> {
                Map<String, Object> orderMap = new HashMap<>();
                orderMap.put("id", order.getId());
                orderMap.put("product", Map.of(
                        "id", order.getProduct().getId(),
                        "name", order.getProduct().getName()
                ));
                orderMap.put("quantity", order.getQuantity());
                orderMap.put("status", order.getStatus().toString());
                orderMap.put("createdAt", order.getCreatedAt());
                return orderMap;
            }).toList();

            return ResponseEntity.ok(responseOrders);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> requestBody,
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();

        try {
            String userEmail = jwtUtil.extractEmail(token.replace("Bearer ", ""));
            User user = userRepo.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!jwtUtil.validateToken(token.replace("Bearer ", ""), userEmail)) {
                response.put("success", false);
                response.put("message", "Invalid or expired token!");
                return ResponseEntity.status(401).body(response);
            }

            if (user.getRole() != Role.productManager) {
                response.put("success", false);
                response.put("message", "Only product managers can update order status.");
                return ResponseEntity.status(403).body(response);
            }

            String newStatus = requestBody.get("status");
            if (newStatus == null) {
                response.put("success", false);
                response.put("message", "Status must be provided!");
                return ResponseEntity.status(400).body(response);
            }

            Order order = orderRepo.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            order.setStatus(OrderStatus.valueOf(newStatus));
            orderRepo.save(order);

            response.put("success", true);
            response.put("message", "Order status updated successfully!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
