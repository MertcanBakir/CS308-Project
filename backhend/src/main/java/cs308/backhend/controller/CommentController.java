package cs308.backhend.controller;

import cs308.backhend.model.Comment;
import cs308.backhend.model.Product;
import cs308.backhend.model.Role;
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

        try {
            String userEmail = jwtUtil.extractEmail(token.replace("Bearer ", ""));
            User user = userRepo.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!jwtUtil.validateToken(token.replace("Bearer ", ""), userEmail)) {
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
            comment.setApproved(null);

            commentRepository.save(comment);

            response.put("success", true);
            response.put("message", "Yorum başarıyla kaydedildi!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Hata oluştu: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/all")
    @CrossOrigin
    public ResponseEntity<?> getAllComments(
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();

        try {
            String userEmail = jwtUtil.extractEmail(token.replace("Bearer ", ""));
            User user = userRepo.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!jwtUtil.validateToken(token.replace("Bearer ", ""), userEmail)) {
                response.put("success", false);
                response.put("message", "Invalid or expired token!");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            if (user.getRole() != Role.productManager) {
                response.put("success", false);
                response.put("message", "Only product managers can access all comments.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            List<Comment> comments = commentRepository.findAll();

            List<Map<String, Object>> commentList = comments.stream().map(comment -> {
                Map<String, Object> commentMap = new HashMap<>();
                commentMap.put("id", comment.getId());
                commentMap.put("content", comment.getContent());
                commentMap.put("rating", comment.getRating());
                commentMap.put("approved", comment.getApproved());
                commentMap.put("createdAt", comment.getCreatedAt());
                commentMap.put("productName", comment.getProduct().getName());
                commentMap.put("userFullName", comment.getUser().getFullName());
                commentMap.put("userId", comment.getUser().getId());
                return commentMap;
            }).toList();

            return ResponseEntity.ok(commentList);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Hata oluştu: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PatchMapping("/approve/{commentId}")
    public ResponseEntity<Map<String, Object>> approveComment(
            @PathVariable Long commentId,
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Boolean> requestBody) {

        Map<String, Object> response = new HashMap<>();

        String userEmail = jwtUtil.extractEmail(token.replace("Bearer ", ""));
        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!jwtUtil.validateToken(token.replace("Bearer ", ""), userEmail)) {
            response.put("success", false);
            response.put("message", "Invalid or expired token!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        if (user.getRole() != Role.productManager) {
            response.put("success", false);
            response.put("message", "Only product managers can approve or reject comments.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        Boolean approved = requestBody.get("approved");

        if (approved == null) {
            response.put("success", false);
            response.put("message", "Approval status (approved) must be provided!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        comment.setApproved(approved);
        commentRepository.save(comment);

        response.put("success", true);
        response.put("message", approved ? "Comment approved successfully!" : "Comment rejected successfully!");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Map<String, Object>> getComments(@PathVariable Long productId) {
        List<Comment> comments = commentRepository.findByProduct_Id(productId);

        List<Map<String, Object>> commentList = comments.stream()
                .map(comment -> {
                    Map<String, Object> commentMap = new HashMap<>();
                    commentMap.put("id", comment.getId());
                    commentMap.put("rating", comment.getRating());
                    commentMap.put("createdAt", comment.getCreatedAt());
                    commentMap.put("fullName", comment.getUser().getFullName());
                    if (Boolean.TRUE.equals(comment.getApproved())) {
                        commentMap.put("content", comment.getContent());
                    } else {
                        commentMap.put("content", ""); // reddedilmiş ya da null olanlar
                    }
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

        String userEmail = jwtUtil.extractEmail(token.replace("Bearer ", ""));
        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean hasPurchased = orderRepo.existsByUser_IdAndProduct_Id(user.getId(), productId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("canComment", hasPurchased);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/unapproved")
    public ResponseEntity<Map<String, Object>> getUnapprovedComments(
            @RequestHeader("Authorization") String token) {

        Map<String, Object> response = new HashMap<>();

        String userEmail = jwtUtil.extractEmail(token.replace("Bearer ", ""));
        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!jwtUtil.validateToken(token.replace("Bearer ", ""), userEmail)) {
            response.put("success", false);
            response.put("message", "Invalid or expired token!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        if (user.getRole() != Role.productManager) {
            response.put("success", false);
            response.put("message", "Only product managers can view unapproved comments.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        List<Comment> comments = commentRepository.findByApprovedFalse();

        List<Map<String, Object>> commentList = comments.stream().map(comment -> {
            Map<String, Object> commentMap = new HashMap<>();
            commentMap.put("id", comment.getId());
            commentMap.put("content", comment.getContent());
            commentMap.put("rating", comment.getRating());
            commentMap.put("productName", comment.getProduct().getName());
            commentMap.put("userFullName", comment.getUser().getFullName());
            commentMap.put("createdAt", comment.getCreatedAt());
            return commentMap;
        }).toList();

        response.put("comments", commentList);

        return ResponseEntity.ok(response);
    }
}
