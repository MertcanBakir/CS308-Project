package cs308.backhend.controller;

import cs308.backhend.model.User;
import cs308.backhend.repository.UserRepo;
import cs308.backhend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final UserRepo userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    @PostMapping
    public ResponseEntity<Map<String, Object>> login(@RequestBody User loginRequest) {
        Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("success", false));
        }

        User user = userOptional.get();
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("success", false));
        }

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(), loginRequest.getPassword()
        ));

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("token", token);
        response.put("email", user.getEmail());
        response.put("address", user.getAddress());
        response.put("fullname", user.getFullName());
        response.put("card", user.getCreditCardLast4Digits());


        return ResponseEntity.ok(response);
    }
}