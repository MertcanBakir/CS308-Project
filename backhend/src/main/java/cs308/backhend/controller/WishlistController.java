package cs308.backhend.controller;

import cs308.backhend.model.User;
import cs308.backhend.model.Wishlist;
import cs308.backhend.repository.UserRepo;
import cs308.backhend.security.JwtUtil;
import cs308.backhend.service.WishListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishListService wishListService;
    private final UserRepo userRepo;
    private final JwtUtil jwtUtil;

    // Ürünü wishlist'e ekle
    @PostMapping("/add/{productId}")
    public ResponseEntity<?> addToWishlist(@PathVariable Long productId,
                                           @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        User user = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        Wishlist wishlist = wishListService.addToWishlist(user.getId(), productId, 1L);
        return ResponseEntity.ok(wishlist);
    }

    // Kullanıcının tüm wishlist öğelerini getir
    @GetMapping
    public ResponseEntity<List<Wishlist>> getWishlist(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        User user = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        List<Wishlist> wishlist = wishListService.getWishlistForUser(user.getId());
        return ResponseEntity.ok(wishlist);
    }

    // Ürünü wishlist'ten çıkar
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<String> removeFromWishlist(@PathVariable Long productId,
                                                     @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        User user = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        wishListService.removeFromWishlist(user.getId(), productId);
        return ResponseEntity.ok("Product removed from wishlist");
    }
}
