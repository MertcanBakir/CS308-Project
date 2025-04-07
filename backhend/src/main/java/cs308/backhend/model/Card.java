package cs308.backhend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "cards")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private String Name;

    @Column(nullable = true)
    private String Surname;

    @Column(nullable = true)
    private String CardNumber;

    @Column(nullable = true)
    private String cvv;

    @Column(nullable = true)
    private String cardExpiryDate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

}
