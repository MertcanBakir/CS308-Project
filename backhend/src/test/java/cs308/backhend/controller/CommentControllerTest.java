package cs308.backhend.controller;

import cs308.backhend.model.Comment;
import cs308.backhend.model.Product;
import cs308.backhend.model.Role;
import cs308.backhend.model.User;
import cs308.backhend.repository.CommentRepository;
import cs308.backhend.repository.ProductRepo;
import cs308.backhend.repository.UserRepo;
import cs308.backhend.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(CommentControllerTest.MockConfig.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private JwtUtil jwtUtil;

    @TestConfiguration
    static class MockConfig {
        @Bean public CommentRepository commentRepository() { return Mockito.mock(CommentRepository.class); }
        @Bean public UserRepo userRepo() { return Mockito.mock(UserRepo.class); }
        @Bean public ProductRepo productRepo() { return Mockito.mock(ProductRepo.class); }
        @Bean public JwtUtil jwtUtil() { return Mockito.mock(JwtUtil.class); }
    }

    @Test
    @DisplayName("User successfully adds a comment and rating")
    void userAddsCommentSuccessfully() throws Exception {
        String token = "Bearer faketoken";
        String email = "test@example.com";

        User user = new User();
        user.setEmail(email);
        user.setRole(Role.User);

        Product product = new Product();
        product.setId(1L);

        when(jwtUtil.extractEmail(token.replace("Bearer ", ""))).thenReturn(email);
        when(jwtUtil.validateToken(token.replace("Bearer ", ""), email)).thenReturn(true);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
        when(productRepo.findById(1L)).thenReturn(Optional.of(product));

        ObjectMapper mapper = new ObjectMapper();
        String requestBody = mapper.writeValueAsString(
                new java.util.HashMap<>() {{
                    put("productId", 1);
                    put("rating", 5);
                    put("content", "Great product!");
                }}
        );

        mockMvc.perform(post("/comments/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(requestBody))
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

        when(jwtUtil.extractEmail(token.replace("Bearer ", ""))).thenReturn(email);
        when(jwtUtil.validateToken(token.replace("Bearer ", ""), email)).thenReturn(true);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(manager));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        ObjectMapper mapper = new ObjectMapper();
        String requestBody = mapper.writeValueAsString(new java.util.HashMap<>() {{ put("approved", true); }});

        mockMvc.perform(patch("/comments/approve/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Comment approved successfully!"));

        verify(commentRepository, times(1)).save(comment);
    }

    @Test
    @DisplayName("Unauthorized user cannot approve a comment")
    void unauthorizedUserCannotApproveComment() throws Exception {
        String token = "Bearer faketoken";
        String email = "user@example.com";

        User normalUser = new User();
        normalUser.setEmail(email);
        normalUser.setRole(Role.User);

        when(jwtUtil.extractEmail(token.replace("Bearer ", ""))).thenReturn(email);
        when(jwtUtil.validateToken(token.replace("Bearer ", ""), email)).thenReturn(true);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(normalUser));

        ObjectMapper mapper = new ObjectMapper();
        String requestBody = mapper.writeValueAsString(new java.util.HashMap<>() {{ put("approved", true); }});

        mockMvc.perform(patch("/comments/approve/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(requestBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Only product managers can approve or reject comments."));
    }
}

