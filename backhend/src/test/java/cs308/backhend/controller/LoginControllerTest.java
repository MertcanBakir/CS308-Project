package cs308.backhend.controller;

import cs308.backhend.model.Address;
import cs308.backhend.model.Card;
import cs308.backhend.model.Role;
import cs308.backhend.model.User;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoginController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(LoginControllerTest.MockConfig.class)
class LoginControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepo userRepo;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    static final String LOGIN_URL = "/login";
    static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Başarılı giriş")
    void testSuccessfulLogin_user() throws Exception {
        User user = createMockUser("user@test.com", "encodedpass", Role.User);
        String requestBody = objectMapper.writeValueAsString(createLoginRequest("user@test.com", "rawpass"));

        when(userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("rawpass", "encodedpass")).thenReturn(true);
        when(jwtUtil.generateToken(user.getEmail(), "User")).thenReturn("mocktoken");

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocktoken"))
                .andExpect(jsonPath("$.role").value("User"));
    }

    @Test
    @DisplayName("Geçersiz email ile login başarısız")
    void testLoginFailsWithInvalidEmail() throws Exception {
        when(userRepo.findByEmail("notfound@test.com")).thenReturn(Optional.empty());
        String body = objectMapper.writeValueAsString(createLoginRequest("notfound@test.com", "any"));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Hatalı şifre ile giriş başarısız")
    void testLoginFailsWithWrongPassword() throws Exception {
        User user = createMockUser("user@test.com", "encodedpass", Role.User);
        when(userRepo.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encodedpass")).thenReturn(false);

        String body = objectMapper.writeValueAsString(createLoginRequest("user@test.com", "wrong"));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Kullanıcının adres ve kart bilgileri dönmeli")
    void testLoginReturnsCardAndAddress() throws Exception {
        User user = createMockUser("user@test.com", "encodedpass", Role.User);
        Address addr = new Address(); addr.setAddress("Test Address");
        Card card = new Card(); card.setCardNumber("1234567812345678");
        user.setAddresses(List.of(addr));
        user.setCards(List.of(card));

        when(userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("raw", "encodedpass")).thenReturn(true);
        when(jwtUtil.generateToken(user.getEmail(), "User")).thenReturn("token");

        String body = objectMapper.writeValueAsString(createLoginRequest("user@test.com", "raw"));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.addresses[0]").value("Test Address"))
                .andExpect(jsonPath("$.card[0]").value("5678"));
    }
    @Test
    @DisplayName("Giriş sonrası fullname bilgisi dönmeli")
    void testSuccessfulLoginReturnsFullName() throws Exception {
        User user = createMockUser("user@test.com", "encodedpass", Role.User);
        user.setFullName("Efecan Kasapoğlu");

        when(userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("rawpass", "encodedpass")).thenReturn(true);
        when(jwtUtil.generateToken(user.getEmail(), "User")).thenReturn("mocktoken");

        User loginRequest = new User();
        loginRequest.setEmail("user@test.com");
        loginRequest.setPassword("rawpass");

        String requestBody = objectMapper.writeValueAsString(loginRequest);

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullname").value("Efecan Kasapoğlu"));
    }

    private User createMockUser(String email, String encodedPassword, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setRole(role);
        user.setAddresses(List.of());
        user.setCards(List.of());
        return user;
    }

    private Map<String, String> createLoginRequest(String email, String password) {
        return Map.of("email", email, "password", password);
    }

    @TestConfiguration
    static class MockConfig {
        @Bean public UserRepo userRepo() { return Mockito.mock(UserRepo.class); }
        @Bean public JwtUtil jwtUtil() { return Mockito.mock(JwtUtil.class); }
        @Bean public AuthenticationManager authenticationManager() { return Mockito.mock(AuthenticationManager.class); }
        @Bean public BCryptPasswordEncoder passwordEncoder() { return Mockito.mock(BCryptPasswordEncoder.class); }
    }
}
