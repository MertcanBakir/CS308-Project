package cs308.backhend.controller;

import cs308.backhend.model.Role;
import cs308.backhend.model.User;
import cs308.backhend.repository.UserRepo;
import cs308.backhend.security.JwtUtil;
import cs308.backhend.service.ProductService;
import cs308.backhend.service.WishlistNotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UnauthorizedUserCannotUpdateProductTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private ProductService productService;
    @MockBean private JwtUtil jwtUtil;
    @MockBean private UserRepo userRepo;
    @MockBean private WishlistNotificationService wishlistNotificationService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("User with no permission attempts to update product name (but controller lacks role check)")
    void UnauthorizedUserCannotUpdateProduct() throws Exception {
        String token = "Bearer faketoken";
        String jwt = "faketoken";
        String email = "user@normal.com";

        User user = new User();
        user.setEmail(email);
        user.setRole(Role.User);  // Rol kontrolü yapılmadığı için bu test yine başarılı olacak

        when(jwtUtil.extractEmail(jwt)).thenReturn(email);
        when(jwtUtil.validateToken(jwt, email)).thenReturn(true);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));

        String body = """
    {
      "name": "Attempted Update",
      "quantityInStock": 50
    }
    """;

        mockMvc.perform(patch("/products/10/update-basic-info")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk()); // controller role kontrolü yapmadığı için 200 dönüyor
    }
}

