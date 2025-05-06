package cs308.backhend.controller;

import cs308.backhend.model.RealWishlist;
import cs308.backhend.model.User;
import cs308.backhend.security.JwtUtil;
import cs308.backhend.service.RealWishlistService;
import cs308.backhend.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/real-wishlist")
@RequiredArgsConstructor
public class RealWishlistController {

    private final RealWishlistService realWishlistService;
    private final JwtUtil jwtUtil;
    private final UserRepo userRepo;

    // GET /real-wishlist → Kullanıcının tüm wishlist ürünleri
    @GetMapping
    public ResponseEntity<List<RealWishlist>> getRealWishlist(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        User user = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        List<RealWishlist> list = realWishlistService.getRealWishlist(user.getId());
        return ResponseEntity.ok(list);
    }

    // POST /real-wishlist/add/{productId}
    @PostMapping("/add/{productId}")
    public ResponseEntity<?> addToRealWishlist(
            @PathVariable Long productId,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        User user = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        RealWishlist added = realWishlistService.addToRealWishlist(user.getId(), productId);
        return ResponseEntity.ok(added);
    }

    // DELETE /real-wishlist/remove/{productId}
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<?> removeFromRealWishlist(
            @PathVariable Long productId,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        User user = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        realWishlistService.removeFromRealWishlist(user.getId(), productId);
        return ResponseEntity.ok("Product removed from real wishlist.");
    }
}
