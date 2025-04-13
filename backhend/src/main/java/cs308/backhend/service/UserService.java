package cs308.backhend.service;

import cs308.backhend.model.Address;
import cs308.backhend.model.Role;
import cs308.backhend.model.User;
import cs308.backhend.model.Card;
import cs308.backhend.repository.AddressRepo;
import cs308.backhend.repository.CardRepo;
import cs308.backhend.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepo userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AddressRepo addressRepository;
    private final CardRepo cardRepository;



    @Autowired
    public UserService(UserRepo userRepository,AddressRepo addressRepository, CardRepo cardRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.addressRepository = addressRepository;
        this.cardRepository = cardRepository;
    }

    public List<Address> getUserAddresses(Long userId) {
        return addressRepository.findByUserId(userId);
    }

    public List<Card> getUserCards(Long userId) {
        return cardRepository.findByUserId(userId);
    }

    @Transactional
    public ResponseEntity<String> registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists.");
        }

        if (user.getRole() == null) {
            user.setRole(Role.User);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (user.getAddresses() != null) {
            for (Address address : user.getAddresses()) {
                address.setUser(user);
            }
        }
        userRepository.save(user);

        return ResponseEntity.ok("Registration successful.");
    }

    public void saveAddress(Address address) {
        addressRepository.save(address);
    }

    public void deleteAddress(Address address) {
        addressRepository.delete(address);
    }
}