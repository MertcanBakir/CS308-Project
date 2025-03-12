package cs308.backhend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepo userRepository;


    @Autowired
    public UserService(UserRepo userRepository) {
        this.userRepository = userRepository;

    }

    public ResponseEntity<String> registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists.");
        }
        user.setPassword((user.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("Registration successful.");
    }

}
