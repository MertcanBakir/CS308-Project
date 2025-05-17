package cs308.backhend.controller;

import cs308.backhend.model.*;
import cs308.backhend.repository.AddressRepo;
import cs308.backhend.repository.CardRepo;
import cs308.backhend.repository.OrderRepo;
import cs308.backhend.repository.UserRepo;
import cs308.backhend.security.JwtUtil;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import cs308.backhend.dto.PasswordChangeRequest;



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
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserRepo userRepo,
                          AddressRepo addressRepository,
                          CardRepo creditCardRepository,
                          OrderRepo orderRepo,
                          JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.addressRepository = addressRepository;
        this.creditCardRepository = creditCardRepository;
        this.orderRepo = orderRepo;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
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
            userData.put("id", user.getId());  // 👈 Kullanıcı ID eklendi
            userData.put("email", user.getEmail());
            userData.put("fullName", user.getFullName());


            List<String> addresses = addressRepository.findByUserId(user.getId())
                    .stream()
                    .map(Address::getAddress)
                    .collect(Collectors.toList());
            userData.put("addresses", addresses);


            List<String> cards = creditCardRepository.findByUserId(user.getId())
                    .stream()
                    .map(Card::getLast4Digits)
                    .collect(Collectors.toList());
            userData.put("cards", cards);

            userData.put("passwordInfo", "******** (hidden for security)");




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

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String token,
                                            @RequestBody PasswordChangeRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String userEmail = jwtUtil.extractEmail(token);
            if (!jwtUtil.validateToken(token, userEmail)) {
                response.put("success", false);
                response.put("message", "Invalid or expired token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Optional<User> userOptional = userRepo.findByEmail(userEmail);
            if (userOptional.isEmpty()) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            User user = userOptional.get();


            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                response.put("success", false);
                response.put("message", "Old password is incorrect");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }


            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepo.save(user);

            response.put("success", true);
            response.put("message", "Password changed successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Unexpected error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
