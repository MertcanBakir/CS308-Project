package cs308.backhend.controller;

import cs308.backhend.model.*;
import cs308.backhend.repository.AddressRepo;
import cs308.backhend.repository.CardRepo;
import cs308.backhend.repository.CommentRepository;
import cs308.backhend.repository.OrderRepo;
import cs308.backhend.repository.UserRepo;
import cs308.backhend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/profile")
public class UserController {

    private final UserRepo userRepo;
    private final AddressRepo addressRepository;
    private final CardRepo creditCardRepository;
    private final OrderRepo orderRepo;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserController(UserRepo userRepo, AddressRepo addressRepository, CardRepo creditCardRepository, OrderRepo orderRepo,JwtUtil jwtUtil){
        this.userRepo = userRepo;
        this.addressRepository = addressRepository;
        this.creditCardRepository = creditCardRepository;
        this.orderRepo = orderRepo;
        this.jwtUtil = jwtUtil;
    }
    @Transactional(readOnly = true)
    @GetMapping("/full")
    public ResponseEntity<?> getFullProfile(@RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            String userEmail = jwtUtil.extractEmail(token);
            if (!jwtUtil.validateToken(token, userEmail)) {
                response.put("success", false);
                response.put("message", "Invalid or expired token");
                return ResponseEntity.status(401).body(response);
            }

            Optional<User> userOptional = userRepo.findByEmail(userEmail);
            if (userOptional.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(401).body(response);
            }

            User user = userOptional.get();

            Map<String, Object> userData = new HashMap<>();
            userData.put("email", user.getEmail());
            userData.put("fullName", user.getFullName());

            // Addresses
            List<String> addresses = addressRepository.findByUserId(user.getId())
                    .stream()
                    .map(Address::getAddress)
                    .collect(Collectors.toList());
            userData.put("addresses", addresses);

            // Cards
            List<String> cards = creditCardRepository.findByUserId(user.getId())
                    .stream()
                    .map(Card::getLast4Digits)
                    .collect(Collectors.toList());
            userData.put("cards", cards);

            // Orders
            List<Order> orders = orderRepo.findByUser_Id(user.getId());
            List<Map<String, Object>> ordersList = orders.stream().map(order -> {
                Map<String, Object> orderMap = new HashMap<>();
                orderMap.put("id", order.getId());
                orderMap.put("productName", order.getProduct().getName());
                orderMap.put("cardLast4", order.getCard().getLast4Digits());
                orderMap.put("addressText", order.getAddress().getAddress());
                orderMap.put("quantity", order.getQuantity());
                orderMap.put("status", order.getStatus().toString());
                orderMap.put("createdAt", order.getCreatedAt());
                orderMap.put("productImageUrl", order.getProduct().getImageUrl());

                Invoice invoice = order.getInvoice();
                orderMap.put("invoiceId", invoice != null ? invoice.getId() : null);

                return orderMap;
            }).collect(Collectors.toList());
            userData.put("orders", ordersList);

            return ResponseEntity.ok(userData);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}