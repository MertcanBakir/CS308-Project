package cs308.backhend;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//@CrossOrigin(origins = "http://localhost:3000") // React ile bağlantıyı açıyoruz
@RestController()
@RequiredArgsConstructor
public class Controller {

    private final UserService userService;

    public ResponseEntity<String> register(@RequestBody User user) {
        return userService.registerUser(user);
    }
}
