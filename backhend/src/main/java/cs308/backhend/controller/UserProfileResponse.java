package cs308.backhend.controller;

import java.util.List;

public class UserProfileResponse {
    private String email;
    private String fullName;
    private List<String> addresses;
    private List<String> cards;

    // Getters & setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public List<String> getAddresses() { return addresses; }
    public void setAddresses(List<String> addresses) { this.addresses = addresses; }

    public List<String> getCards() { return cards; }
    public void setCards(List<String> cards) { this.cards = cards; }
}
