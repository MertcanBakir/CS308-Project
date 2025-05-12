package cs308.backhend.controller;

import cs308.backhend.model.*;
import cs308.backhend.repository.*;
import cs308.backhend.security.JwtUtil;
import cs308.backhend.service.OrderService;
import cs308.backhend.service.ProductService;
import cs308.backhend.model.Role;
import cs308.backhend.service.WishListService;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.time.LocalDateTime;
import java.math.BigDecimal;


@RestController
public class OrderController {

    private final OrderService orderService;
    private final OrderRepo orderRepo;
    private final UserRepo userRepo;
    private final ProductService productService;
    private final CardRepo cardRepo;
    private final AddressRepo addressRepo;
    private final WishListService wishListService;
    private final JwtUtil jwtUtil;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    public OrderController(OrderService orderService, OrderRepo orderRepo, UserRepo userRepo,
                           ProductService productService, CardRepo cardRepo,
                           AddressRepo addressRepo, WishListService wishListService,
                           JwtUtil jwtUtil) {
        this.orderService = orderService;
        this.orderRepo = orderRepo;
        this.userRepo = userRepo;
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
            User user = userRepo.findByEmail(userEmail).orElseThrow();

            if (!jwtUtil.validateToken(token.replace("Bearer ", ""), userEmail)) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Invalid token"));
            }

            List<Integer> wishlistIds = (List<Integer>) requestBody.get("wishlist_ids");
            Long cardId = Long.parseLong(requestBody.get("card_id").toString());
            Long addressId = Long.parseLong(requestBody.get("address_id").toString());

            Card card = cardRepo.findById(cardId).orElseThrow();
            Address address = addressRepo.findById(addressId).orElseThrow();

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

                productService.decrementStock(product, quantity);
                wishListService.removeFromWishlist(user.getId(), product.getId());

                orderList.add(order);
            }

            for (Order order : orderList) {
                orderRepo.save(order);
            }

            orderService.generateInvoiceAndSendEmail(orderList, user, "processed");

            return ResponseEntity.ok(Map.of("success", true, "message", "Orders placed and invoice sent"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PatchMapping("/orders/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId, @RequestHeader("Authorization") String token) {
        try {
            String email = jwtUtil.extractEmail(token.replace("Bearer ", ""));
            User user = userRepo.findByEmail(email).orElseThrow();
            Order order = orderRepo.findById(orderId).orElseThrow();

            if (!order.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(Map.of("message", "Unauthorized"));
            }

            if (order.getStatus() != OrderStatus.PROCESSING) {
                return ResponseEntity.status(400).body(Map.of("message", "Only PROCESSING orders can be cancelled."));
            }

            order.setStatus(OrderStatus.CANCELLED);
            orderRepo.save(order);

            Product product = order.getProduct();
            product.setQuantityInStock(product.getQuantityInStock() + order.getQuantity());
            productRepo.save(product);

            orderService.generateInvoiceAndSendEmail(List.of(order), user, "cancelled");

            return ResponseEntity.ok(Map.of("message", "Order cancelled and invoice sent."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/all-order")
    public ResponseEntity<?> getAllOrders(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            String userEmail = jwtUtil.extractEmail(jwt);
            User user = userRepo.findByEmail(userEmail).orElseThrow();

            if (!jwtUtil.validateToken(jwt, userEmail)) {
                return ResponseEntity.status(401).body(Map.of("success", false, "message", "Invalid token"));
            }

            if (user.getRole() != Role.productManager) {
                return ResponseEntity.status(403).body(Map.of("success", false, "message", "Unauthorized access"));
            }

            List<Order> orders = orderRepo.findAll();

            List<Map<String, Object>> responseOrders = orders.stream().map(order -> {
                Map<String, Object> orderMap = new HashMap<>();
                BigDecimal price = order.getProduct().getPrice();

                orderMap.put("id", order.getId());  // Delivery ID
                orderMap.put("userId", order.getUser() != null ? order.getUser().getId() : null);  // Customer ID

                orderMap.put("product", Map.of(
                        "id", order.getProduct().getId(),
                        "name", order.getProduct().getName(),
                        "price", price
                ));

                orderMap.put("quantity", order.getQuantity());
                orderMap.put("totalPrice", price != null ? price.multiply(BigDecimal.valueOf(order.getQuantity())) : null);

                orderMap.put("address", Map.of(
                        "id", order.getAddress().getId(),
                        "address", order.getAddress().getAddress()
                ));

                orderMap.put("status", order.getStatus().toString());
                orderMap.put("createdAt", order.getCreatedAt());
                return orderMap;
            }).toList();

            return ResponseEntity.ok(responseOrders);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }




    @PatchMapping("/orders/{orderId}/refund")
    public ResponseEntity<?> refundOrder(@PathVariable Long orderId, @RequestHeader("Authorization") String token) {
        try {
            String email = jwtUtil.extractEmail(token.replace("Bearer ", ""));
            User user = userRepo.findByEmail(email).orElseThrow();
            Order order = orderRepo.findById(orderId).orElseThrow();

            if (!order.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(Map.of("message", "Unauthorized"));
            }

            if (order.getStatus() != OrderStatus.DELIVERED ||
                    order.getCreatedAt().isBefore(LocalDateTime.now().minusDays(30))) {
                return ResponseEntity.status(400).body(Map.of("message", "Refund not allowed."));
            }

            order.setStatus(OrderStatus.REFUNDED);
            orderRepo.save(order);

            Product product = order.getProduct();
            product.setQuantityInStock(product.getQuantityInStock() + order.getQuantity());
            productRepo.save(product);

            orderService.generateInvoiceAndSendEmail(List.of(order), user, "refunded");

            BigDecimal refundAmount = product.getPrice().multiply(BigDecimal.valueOf(order.getQuantity()));
            return ResponseEntity.ok(Map.of("message", "Refunded", "amount", refundAmount));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
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

    @Transactional(readOnly = true)
    @GetMapping("/all-order-SL")
    public ResponseEntity<?> getAllOrdersSalesManager(@RequestHeader("Authorization") String token) {
        System.out.println("Extracted role: " + jwtUtil.extractRole(token));
        if (!Role.valueOf(jwtUtil.extractRole(token)).equals(Role.salesManager)) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "You do not have permission to access this resource");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        List<Order> orders = orderRepo.findAll();
        List<Map<String, Object>> responseList = new ArrayList<>();

        for (Order order : orders) {
            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("quantity", order.getQuantity());
            orderMap.put("time", order.getCreatedAt());
            orderMap.put("productId", order.getProduct().getId());
            orderMap.put("productName", order.getProduct().getName());
            orderMap.put("price", order.getProduct().getPrice());

            responseList.add(orderMap);
        }
        return ResponseEntity.ok(responseList);
    }
}
