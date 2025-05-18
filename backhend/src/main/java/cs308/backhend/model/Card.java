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
    private String cardNumber;

    public void setCardNumber(String cardNumber) {
        try {
            this.cardNumber = AESUtil.encrypt(cardNumber);
        } catch (Exception e) {
            throw new RuntimeException("Encryption error", e);
        }
    }

    public String getCardNumber() {
        try {
            return AESUtil.decrypt(this.cardNumber);
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

    public String getLast4Digits() {
        try {
            String decrypted = AESUtil.decrypt(this.cardNumber);
            if (decrypted != null && decrypted.length() >= 4) {
                return decrypted.substring(decrypted.length() - 4);
            } else {
                return "****";
            }
        } catch (Exception e) {
            return "****";
        }
    }
    // Card.java
    @Override
    public String toString() {
        return "Card{" +
                "cardNumber='" + getCardNumber() + '\'' +
                ", cardName='" + Name + '\'' +
                ", userId=" + (user != null ? user.getId() : "null") +
                '}';
    }


}
