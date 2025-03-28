package cs308.backhend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = true)
    private String address;

    @Column(nullable = true)
    private String creditCardLast4Digits;

    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private Role role;
}