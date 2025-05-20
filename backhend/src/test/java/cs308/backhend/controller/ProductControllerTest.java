package cs308.backhend.controller;

import cs308.backhend.model.Product;
import cs308.backhend.model.Role;
import cs308.backhend.model.User;
import cs308.backhend.security.JwtUtil;
import cs308.backhend.service.ProductService;
import cs308.backhend.service.WishlistNotificationService;
import cs308.backhend.repository.UserRepo;
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

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProductControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private ProductService productService;
    @MockBean private JwtUtil jwtUtil;
    @MockBean private UserRepo userRepo;
    @MockBean private WishlistNotificationService wishlistNotificationService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Product Manager successfully adds a product")
    void productManagerAddsProductSuccessfully() throws Exception {
        String token = "Bearer faketoken";
        String email = "manager@sephora.com";

        User manager = new User();
        manager.setEmail(email);
        manager.setRole(Role.productManager);

        when(jwtUtil.extractEmail("faketoken")).thenReturn(email);
        when(jwtUtil.validateToken("faketoken", email)).thenReturn(true);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(manager));

        Map<String, Object> payload = Map.of(
                "name", "Lipstick",
                "serialNumber", "LIP-001",
                "model", "Matte",
                "description", "Long-lasting lipstick",
                "distributorInfo", "Sephora Co.",
                "imageUrl", "http://img.com/lip.png",
                "warrantyStatus", true,
                "quantityInStock", 100,
                "categoryIds", List.of(1, 2)
        );

        mockMvc.perform(post("/products/add")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Unauthorized user cannot add product")
    void unauthorizedUserCannotAddProduct() throws Exception {
        String token = "Bearer testtoken";
        String email = "user@sephora.com";

        User user = new User();
        user.setEmail(email);
        user.setRole(Role.User);

        when(jwtUtil.extractEmail("testtoken")).thenReturn(email);
        when(jwtUtil.validateToken("testtoken", email)).thenReturn(true);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));

        Map<String, Object> payload = Map.of("name", "Unauthorized Product");

        mockMvc.perform(post("/products/add")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(payload)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Sales manager updates product price")
    void salesManagerUpdatesPriceSuccessfully() throws Exception {
        String token = "Bearer testtoken";
        String email = "sales@sephora.com";

        User user = new User();
        user.setEmail(email);
        user.setRole(Role.salesManager);

        Product product = new Product();
        product.setId(1L);
        product.setPrice(BigDecimal.valueOf(200.00));

        when(jwtUtil.extractEmail("testtoken")).thenReturn(email);
        when(jwtUtil.validateToken("testtoken", email)).thenReturn(true);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
        when(productService.getProductById(1L)).thenReturn(product);

        Map<String, Object> payload = Map.of("price", 180.00);

        mockMvc.perform(put("/products/1/update-price")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Product price updated successfully")));
    }

    @Test
    @DisplayName("Product name and stock updated successfully")
    void productNameAndStockUpdated() throws Exception {
        Product updatedProduct = new Product();
        updatedProduct.setName("New Name");
        updatedProduct.setQuantityInStock(150);

        Product resultProduct = new Product();
        resultProduct.setId(1L);
        resultProduct.setName("New Name");
        resultProduct.setQuantityInStock(150);

        when(productService.updateProductNameAndStock(1L, "New Name", 150)).thenReturn(resultProduct);

        mockMvc.perform(patch("/products/1/update-basic-info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updatedProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.quantityInStock").value(150));
    }
}
