package cs308.backhend.controller;

import cs308.backhend.model.Product;
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
public class SalesManagerUpdatesNameSuccessfully {



    @Autowired private MockMvc mockMvc;

    @MockBean private ProductService productService;
    @MockBean private JwtUtil jwtUtil;
    @MockBean private UserRepo userRepo;
    @MockBean private WishlistNotificationService wishlistNotificationService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Sales Manager updates product name successfully")
    void SalesManagerUpdatesNameSuccessfully() throws Exception {
        String token = "Bearer testtoken";
        String jwt = "testtoken";
        String email = "sales@manager.com";

        User user = new User();
        user.setEmail(email);
        user.setRole(Role.salesManager);

        Product updatedProduct = new Product();
        updatedProduct.setId(10L);
        updatedProduct.setName("Updated Name");
        updatedProduct.setQuantityInStock(100); // mevcut stock örnek değeri

        when(jwtUtil.extractEmail(jwt)).thenReturn(email);
        when(jwtUtil.validateToken(jwt, email)).thenReturn(true);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
        when(productService.updateProductNameAndStock(10L, "Updated Name", 100))
                .thenReturn(updatedProduct);

        String requestBody = """
        {
          "name": "Updated Name",
          "quantityInStock": 100
        }
        """;

        mockMvc.perform(patch("/products/10/update-basic-info")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.quantityInStock").value(100));
    }
}
