package cs308.backhend.controller;

import cs308.backhend.model.Comment;
import cs308.backhend.model.Product;
import cs308.backhend.model.User;
import cs308.backhend.repository.CommentRepository;
import cs308.backhend.repository.OrderRepo;
import cs308.backhend.repository.ProductRepo;
import cs308.backhend.repository.UserRepo;
import cs308.backhend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ProductRepo productRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addComment(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> requestBody) {

        Map<String, Object> response = new HashMap<>();

        String userEmail = jwtUtil.extractEmail(token);
        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!jwtUtil.validateToken(token, userEmail)) {
            response.put("success", false);
            response.put("message", "Invalid or expired token!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Long productId = Long.valueOf(requestBody.get("productId").toString());
        int rating = Integer.parseInt(requestBody.get("rating").toString());
        String content = requestBody.get("content").toString();

        boolean hasPurchased = orderRepo.hasUserBoughtProduct(user.getId(), productId);

        if (!hasPurchased) {
            response.put("success", false);
            response.put("message", "Sadece satın aldığınız ürünlere yorum yapabilirsiniz.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setProduct(product);
        comment.setContent(content);
        comment.setRating(rating);
        comment.setApproved(true);

        commentRepository.save(comment);

        response.put("success", true);
        response.put("message", "Yorum başarıyla kaydedildi!");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Map<String, Object>> getComments(@PathVariable Long productId) {
        List<Comment> comments = commentRepository.findByProduct_IdAndApprovedTrue(productId);

        List<Map<String, Object>> commentList = comments.stream().map(comment -> {
            Map<String, Object> commentMap = new HashMap<>();
            commentMap.put("id", comment.getId());
            commentMap.put("content", comment.getContent());
            commentMap.put("rating", comment.getRating());
            commentMap.put("createdAt", comment.getCreatedAt());
            commentMap.put("fullName", comment.getUser().getFullName());
            return commentMap;
        }).toList();


        Map<String, Object> response = new HashMap<>();
        response.put("comments", commentList);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/can-comment/{productId}")
    public ResponseEntity<Map<String, Boolean>> canUserComment(
            @RequestHeader("Authorization") String token,
            @PathVariable Long productId) {

        String userEmail = jwtUtil.extractEmail(token);
        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean hasPurchased = orderRepo.existsByUser_IdAndProduct_Id(user.getId(), productId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("canComment", hasPurchased);

        return ResponseEntity.ok(response);
    }

}