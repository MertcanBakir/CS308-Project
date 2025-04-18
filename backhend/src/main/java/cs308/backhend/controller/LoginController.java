package cs308.backhend.controller;

import cs308.backhend.model.Address;
import cs308.backhend.model.Role;
import cs308.backhend.model.User;
import cs308.backhend.repository.UserRepo;
import cs308.backhend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

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

        Role role;
        if (user.getEmail().endsWith("@salesman.com")) {
            role = Role.salesManager;
        } else if (user.getEmail().endsWith("@prodman.com")) {
            role = Role.productManager;
        } else if (user.getRole() != null) {
            role = user.getRole();
        } else {
            role = Role.User;
        }

        String token = jwtUtil.generateToken(user.getEmail(), role.name());

        List<String> addressList = user.getAddresses().stream()
                .map(Address::getAddress)
                .collect(Collectors.toList());

        List<String> cardLast4Digits = user.getCards().stream()
                .map(card -> {
                    String fullNumber = card.getCardNumber();
                    return fullNumber != null && fullNumber.length() >= 4
                            ? fullNumber.substring(fullNumber.length() - 4)
                            : "";
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("token", token);
        response.put("email", user.getEmail());
        response.put("fullname", user.getFullName());
        response.put("card", cardLast4Digits);
        response.put("addresses", addressList);
        response.put("role", role.name());

        return ResponseEntity.ok(response);
    }
}
