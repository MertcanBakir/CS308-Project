package cs308.backhend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import cs308.backhend.security.AESUtil;
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

    public void setCardNumber(String cardNumber) {
        try {
            this.CardNumber = AESUtil.encrypt(cardNumber);
        } catch (Exception e) {
            throw new RuntimeException("Encryption error", e);
        }
    }

    public String getCardNumber() {
        try {
            return AESUtil.decrypt(this.CardNumber);
        } catch (Exception e) {
            throw new RuntimeException("Decryption error", e);
        }
    }

    @Column(nullable = true)
    private String cvv;

    @Column(nullable = true)
    private String cardExpiryDate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

}
