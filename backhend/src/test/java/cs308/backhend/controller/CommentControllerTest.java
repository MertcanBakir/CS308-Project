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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CommentControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private JwtUtil jwtUtil;
    @MockBean private ProductRepo productRepo;
    @MockBean private UserRepo userRepo;
    @MockBean private OrderRepo orderRepo;
    @MockBean private CommentRepository commentRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    private Product mockProduct() {
        Product p = new Product();
        p.setId(1L);
        p.setName("Mock Product");
        p.setSerialNumber("SN-001");
        p.setDescription("A good product");
        p.setQuantityInStock(10);
        p.setPrice(BigDecimal.valueOf(99.99));
        p.setWarrantyStatus(true);
        p.setDistributorInfo("Test Dist");
        p.setImageUrl("http://example.com/img.jpg");
        p.setApproved(true);
        return p;
    }

    @Test
    @DisplayName("User successfully adds a comment and rating")
    void userAddsCommentSuccessfully() throws Exception {
        String token = "Bearer faketoken";
        String email = "test@example.com";

        User user = new User();
        user.setId(100L);
        user.setEmail(email);
        user.setFullName("John Doe");
        user.setRole(Role.User);

        Product product = mockProduct();

        when(jwtUtil.extractEmail("faketoken")).thenReturn(email);
        when(jwtUtil.validateToken("faketoken", email)).thenReturn(true);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
        when(orderRepo.hasUserBoughtProduct(user.getId(), product.getId())).thenReturn(true);
        when(productRepo.findById(1L)).thenReturn(Optional.of(product));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("productId", 1);
        requestBody.put("rating", 5);
        requestBody.put("content", "Great product!");

        mockMvc.perform(post("/comments/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(mapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Yorum başarıyla kaydedildi!"));
    }

    @Test
    @DisplayName("Product manager approves a pending comment")
    void productManagerApprovesComment() throws Exception {
        String token = "Bearer faketoken";
        String email = "manager@example.com";

        User manager = new User();
        manager.setEmail(email);
        manager.setRole(Role.productManager);

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setApproved(null);
        comment.setProduct(mockProduct());
        comment.setUser(manager);

        when(jwtUtil.extractEmail("faketoken")).thenReturn(email);
        when(jwtUtil.validateToken("faketoken", email)).thenReturn(true);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(manager));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        Map<String, Boolean> requestBody = Map.of("approved", true);

        mockMvc.perform(patch("/comments/approve/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(mapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Comment approved successfully!"));

        verify(commentRepository, times(1)).save(comment);
    }

    @Test
    @DisplayName("Unauthorized user cannot approve a comment")
    void unauthorizedUserCannotApproveComment() throws Exception {
        String token = "Bearer faketoken";
        String email = "user@example.com";

        User user = new User();
        user.setEmail(email);
        user.setRole(Role.User);

        when(jwtUtil.extractEmail("faketoken")).thenReturn(email);
        when(jwtUtil.validateToken("faketoken", email)).thenReturn(true);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));

        Map<String, Boolean> requestBody = Map.of("approved", true);

        mockMvc.perform(patch("/comments/approve/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(mapper.writeValueAsString(requestBody)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Only product managers can approve or reject comments."));
    }
}