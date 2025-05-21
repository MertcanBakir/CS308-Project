package cs308.backhend.controller;

import cs308.backhend.model.Product;
import cs308.backhend.model.User;
import cs308.backhend.model.Wishlist;
import cs308.backhend.repository.ProductRepo;
import cs308.backhend.repository.UserRepo;
import cs308.backhend.repository.WishListRepo;
import cs308.backhend.security.JwtUtil;
import cs308.backhend.service.WishListService;

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

@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(CartControllerTest.MockConfig.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WishListRepo wishListRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private WishListService wishListService;

    @TestConfiguration
    static class MockConfig {
        @Bean public WishListRepo wishListRepo() { return Mockito.mock(WishListRepo.class); }
        @Bean public UserRepo userRepo() { return Mockito.mock(UserRepo.class); }
        @Bean public ProductRepo productRepo() { return Mockito.mock(ProductRepo.class); }
        @Bean public JwtUtil jwtUtil() { return Mockito.mock(JwtUtil.class); }
        @Bean public WishListService wishListService() { return Mockito.mock(WishListService.class); }
    }

    @Test
    @DisplayName("Quantity iki kez değiştirildiğinde en son değer kaydedilmeli")
    void testChangeQuantityTwice() throws Exception {
        String token = "Bearer faketoken";
        String email = "test@example.com";

        User user = new User();
        user.setEmail(email);

        Product product = new Product();
        product.setId(1L);

        Wishlist wishlist = Mockito.spy(new Wishlist()); // spy ile setQuantity çağrısını takip ederiz
        wishlist.setId(1L);
        wishlist.setUser(user);
        wishlist.setProduct(product);
        wishlist.setQuantity(1L);

        when(jwtUtil.extractEmail(token)).thenReturn(email);
        when(jwtUtil.validateToken(token, email)).thenReturn(true);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
        when(productRepo.findById(1L)).thenReturn(Optional.of(product));
        when(wishListRepo.findByUserAndProduct(user, product)).thenReturn(Optional.of(wishlist));
        when(wishListRepo.save(any(Wishlist.class))).thenReturn(wishlist);

        ObjectMapper mapper = new ObjectMapper();

        String requestBody1 = mapper.writeValueAsString(
                new java.util.HashMap<>() {{
                    put("product_id", 1);
                    put("quantity", 3);
                }}
        );

        mockMvc.perform(put("/change_quantity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(requestBody1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Product quantity updated in wishlist!"));

        String requestBody2 = mapper.writeValueAsString(
                new java.util.HashMap<>() {{
                    put("product_id", 1);
                    put("quantity", 5);
                }}
        );

        mockMvc.perform(put("/change_quantity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(requestBody2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Product quantity updated in wishlist!"));

        verify(wishlist, times(1)).setQuantity(5L);
    }

    @Test
    @DisplayName("Yeni ürün sepete başarıyla eklenmeli")
    void testAddCart() throws Exception {
        String token = "Bearer faketoken";
        String email = "test@example.com";

        User user = new User();
        user.setEmail(email);

        Product product = Mockito.spy(new Product());
        product.setId(1L);

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setProduct(product);
        wishlist.setQuantity(3L);


        when(jwtUtil.extractEmail(token)).thenReturn(email);
        when(jwtUtil.validateToken(token, email)).thenReturn(true);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
        when(productRepo.findById(1L)).thenReturn(Optional.of(product));
        when(wishListRepo.findByUserAndProduct(user, product)).thenReturn(Optional.empty());
        when(wishListRepo.save(any(Wishlist.class))).thenReturn(wishlist);
        when(productRepo.save(any(Product.class))).thenReturn(product);

        ObjectMapper mapper = new ObjectMapper();
        String requestBody = mapper.writeValueAsString(
                new java.util.HashMap<>() {{
                    put("product_id", 1);
                    put("quantity", 3);
                }}
        );

        mockMvc.perform(post("/add_to_cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Product added to wishlist!"));

        verify(product, times(1)).incrementWishlistCount();
        verify(productRepo, times(1)).save(product);
        verify(wishListRepo, times(1)).save(any(Wishlist.class));
    }

    @Test
    @DisplayName("Sepetteki ürün başarıyla silinmeli")
    void testDeleteFromCartSuccessfully() throws Exception {
        String token = "Bearer faketoken";
        String email = "test@example.com";

        User user = new User();
        user.setEmail(email);

        Product product = Mockito.spy(new Product());
        product.setId(1L);

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setProduct(product);
        wishlist.setQuantity(2L);

        when(jwtUtil.extractEmail(token)).thenReturn(email);
        when(jwtUtil.validateToken(token, email)).thenReturn(true);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
        when(productRepo.findById(1L)).thenReturn(Optional.of(product));
        when(wishListRepo.findByUserAndProduct(user, product)).thenReturn(Optional.of(wishlist));

        ObjectMapper mapper = new ObjectMapper();
        String requestBody = mapper.writeValueAsString(
                new java.util.HashMap<>() {{
                    put("product_id", 1);
                }}
        );

        mockMvc.perform(delete("/delete_from_cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Product removed from wishlist"));

        verify(product, times(1)).decrementWishlistCount();
        verify(productRepo, times(1)).save(product);
        verify(wishListRepo, times(1)).delete(wishlist);
    }
    @Test
    @DisplayName("Token olmadan sepete ürün eklenemez – 400 Bad Request")
    void testAddToCartWithoutTokenReturns400() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String requestBody = mapper.writeValueAsString(
                new java.util.HashMap<>() {{
                    put("product_id", 1);
                    put("quantity", 2);
                }}
        );

        mockMvc.perform(post("/add_to_cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
    @Test
    @DisplayName("Geçersiz token ile sepete ürün eklenemez – 401")
    void testAddToCartWithInvalidTokenReturns401() throws Exception {
        String token = "Bearer faketoken";
        String email = "user@example.com";

        User user = new User();
        user.setEmail(email);

        when(jwtUtil.extractEmail(token)).thenReturn(email);
        when(jwtUtil.validateToken(token, email)).thenReturn(false);

        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));

        ObjectMapper mapper = new ObjectMapper();
        String requestBody = mapper.writeValueAsString(
                new java.util.HashMap<>() {{
                    put("product_id", 1);
                    put("quantity", 2);
                }}
        );

        mockMvc.perform(post("/add_to_cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(requestBody))
                .andExpect(status().isUnauthorized());
    }
}