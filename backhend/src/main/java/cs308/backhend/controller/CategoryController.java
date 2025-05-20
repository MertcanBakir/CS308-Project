package cs308.backhend.controller;

import cs308.backhend.model.Category;
import cs308.backhend.model.Role;
import cs308.backhend.model.User;
import cs308.backhend.repository.UserRepo;
import cs308.backhend.security.JwtUtil;
import cs308.backhend.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final JwtUtil jwtUtil;
    private final UserRepo userRepo;

    public CategoryController(CategoryService categoryService, JwtUtil jwtUtil, UserRepo userRepo) {
        this.categoryService = categoryService;
        this.jwtUtil = jwtUtil;
        this.userRepo = userRepo;
    }

    @Transactional(readOnly = true)
    @GetMapping
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @PostMapping("/add")
    public ResponseEntity<?> addCategory(@RequestBody Category category, @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            String jwt = token.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(jwt);

            User user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!jwtUtil.validateToken(jwt, email)) {
                response.put("success", false);
                response.put("message", "Invalid or expired token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            if (user.getRole() != Role.productManager) {
                response.put("success", false);
                response.put("message", "Only product managers can add categories");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            Category newCategory = categoryService.addCategory(category);
            return ResponseEntity.status(HttpStatus.CREATED).body(newCategory);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "An error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        boolean deleted = categoryService.deleteCategory(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", deleted);
        if (deleted) {
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Category not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody Category categoryDetails, @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();

        try {
            String jwt = token.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(jwt);

            User user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!jwtUtil.validateToken(jwt, email)) {
                response.put("success", false);
                response.put("message", "Invalid or expired token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            if (user.getRole() != Role.productManager) {
                response.put("success", false);
                response.put("message", "Only product managers can update categories");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            Category updatedCategory = categoryService.updateCategory(id, categoryDetails);
            if (updatedCategory != null) {
                return ResponseEntity.ok(updatedCategory);
            } else {
                response.put("success", false);
                response.put("message", "Category not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "An error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}