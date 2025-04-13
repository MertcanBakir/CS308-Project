package cs308.backhend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderSummaryResponse {
    private String productName;
    private String cardLast4;
    private String addressText;
    private int quantity;
    private String status;
    private LocalDateTime createdAt;
    private String productImageUrl;

}
