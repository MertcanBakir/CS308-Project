package cs308.backhend.controller;

import cs308.backhend.model.User;
import cs308.backhend.repository.*;
import cs308.backhend.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PasswordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private JwtUtil jwtUtil;
    @MockBean private UserRepo userRepo;
    @MockBean private PasswordEncoder passwordEncoder;
    @MockBean private AddressRepo addressRepository;
    @MockBean private CardRepo creditCardRepository;
    @MockBean private OrderRepo orderRepo;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Fails when token is invalid")
    void failWhenTokenIsInvalid() throws Exception {
        String token = "Bearer invalidtoken";
        String jwt = "invalidtoken";

        when(jwtUtil.extractEmail(jwt)).thenReturn("user@example.com");
        when(jwtUtil.validateToken(jwt, "user@example.com")).thenReturn(false); // Token geçersiz

        String requestBody = """
        {
          "oldPassword": "old123",
          "newPassword": "new123"
        }
        """;

        mockMvc.perform(put("/profile/change-password")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));
    }
}
