package cs308.backhend.controller;

import cs308.backhend.model.Address;
import cs308.backhend.model.Card;
import cs308.backhend.model.User;
import cs308.backhend.repository.AddressRepo;
import cs308.backhend.repository.CardRepo;
import cs308.backhend.repository.UserRepo;
import cs308.backhend.security.JwtUtil;
import cs308.backhend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class RegisterController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserRepo userRepo;
    private final CardRepo cardRepo;
    private final AddressRepo addressRepo;

    @Autowired
    public RegisterController(UserService userService, JwtUtil jwtUtil, UserRepo userRepo, CardRepo cardRepo, AddressRepo addressRepo){
        this.addressRepo = addressRepo;
        this.cardRepo = cardRepo;
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        return userService.registerUser(user);
    }

    @Transactional(readOnly = true)
    @GetMapping("/addresses")
    public ResponseEntity<Map<String, Object>> getAddresses(@RequestHeader("Authorization") String token) {
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

            long userId = jwtUtil.getUserIdFromToken(token);
            List<Address> addresses = userService.getUserAddresses(userId);

            response.put("success", true);
            response.put("addresses", addresses);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    @PostMapping("/add-address")
    public ResponseEntity<Map<String, Object>> addAddress(@RequestBody Map<String, String> request, @RequestHeader("Authorization") String token) {
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

            String adres = request.get("address");
            if (adres == null || adres.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Adres boş olamaz.");
                return ResponseEntity.badRequest().body(response);
            }

            Address address = new Address();
            address.setAddress(adres);
            address.setUser(user);

            userService.saveAddress(address);

            response.put("success", true);
            response.put("message", "Adres başarıyla eklendi.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/delete-address")
    public ResponseEntity<Map<String, Object>> deleteAddress(@RequestBody Map<String, String> request, @RequestHeader("Authorization") String token) {
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

            String adres = request.get("address_id");
            if (adres == null || adres.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Adres boş olamaz.");
                return ResponseEntity.badRequest().body(response);
            }
            Address address = addressRepo.getReferenceById(Long.parseLong(adres));
            userService.deleteAddress(address);

            response.put("success", true);
            response.put("message", "Adres başarıyla silindi.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @Transactional(readOnly = true)
    @GetMapping("/cards")
    public ResponseEntity<Map<String, Object>> getCards(@RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            String userEmail = jwtUtil.extractEmail(token);
            User user = userRepo.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));

            if (!jwtUtil.validateToken(token, userEmail)) {
                response.put("success", false);
                response.put("message", "Invalid or expired token!");
                return ResponseEntity.status(401).body(response);
            }

            response.put("success", true);
            response.put("cards", user.getCards());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Unexpected error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/add-card")
    public ResponseEntity<Map<String, Object>> addCard(@RequestBody Card newCard, @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            String userEmail = jwtUtil.extractEmail(token);
            User user = userRepo.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));

            if (!jwtUtil.validateToken(token, userEmail)) {
                response.put("success", false);
                response.put("message", "Invalid or expired token!");
                return ResponseEntity.status(401).body(response);
            }

            newCard.setUser(user);
            cardRepo.save(newCard);

            response.put("success", true);
            response.put("message", "Kart başarıyla eklendi.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Unexpected error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/delete-card")
    public ResponseEntity<Map<String, Object>> deleteCard(@RequestBody Map<String, String> request, @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            String userEmail = jwtUtil.extractEmail(token);
            User user = userRepo.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));

            if (!jwtUtil.validateToken(token, userEmail)) {
                response.put("success", false);
                response.put("message", "Invalid or expired token!");
                return ResponseEntity.status(401).body(response);
            }

            String cardIdStr = request.get("card_id");
            if (cardIdStr == null || cardIdStr.isBlank()) {
                response.put("success", false);
                response.put("message", "Kart ID boş olamaz.");
                return ResponseEntity.badRequest().body(response);
            }

            Long cardId = Long.parseLong(cardIdStr);
            Card card = cardRepo.findById(cardId).orElseThrow(() -> new RuntimeException("Kart bulunamadı."));
            cardRepo.delete(card);

            response.put("success", true);
            response.put("message", "Kart başarıyla silindi.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Unexpected error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

}