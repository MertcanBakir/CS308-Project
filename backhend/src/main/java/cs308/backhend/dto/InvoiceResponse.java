package cs308.backhend.dto;

import java.time.LocalDateTime;

public class InvoiceResponse {
    private Long orderId;
    private String customerName;
    private String productName;
    private int quantity;
    private LocalDateTime createdAt;
    private String address;
    private String cardLast4;

    public InvoiceResponse(Long orderId, String customerName, String productName, int quantity,
                           LocalDateTime createdAt, String address, String cardLast4) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.productName = productName;
        this.quantity = quantity;
        this.createdAt = createdAt;
        this.address = address;
        this.cardLast4 = cardLast4;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCardLast4() { return cardLast4; }
    public void setCardLast4(String cardLast4) { this.cardLast4 = cardLast4; }
}