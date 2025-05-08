package cs308.backhend.service;

import cs308.backhend.model.*;
import cs308.backhend.repository.*;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistNotificationService {

    private final RealWishlistRepo realWishlistRepo;
    private final JavaMailSender javaMailSender;

    @Value("${company.name:Sephora Store Team 19}")
    private String companyName;

    @Value("${company.email:support@sephorastore.com}")
    private String companyEmail;

    @Value("${company.phone:+90 212 123 4567}")
    private String companyPhone;

    @Value("${company.website:www.sephorastore.com}")
    private String companyWebsite;

    @Value("${company.address:Istanbul, Turkey}")
    private String companyAddress;

    /**
     * Notifies all users who have a specific product in their wishlist about a price change
     *
     * @param product The product with updated price
     * @param oldPrice The previous price before update
     * @param newPrice The new price after update
     */
    public void notifyUsersOfPriceChange(Product product, BigDecimal oldPrice, BigDecimal newPrice) {
        // Find all wishlist items containing this product
        List<RealWishlist> wishlistItems = realWishlistRepo.findByProduct(product);

        if (wishlistItems.isEmpty()) {
            return; // No users have this product in their wishlist
        }

        // Calculate price change percentage for better user communication
        BigDecimal priceChangePercentage = calculatePriceChangePercentage(oldPrice, newPrice);
        boolean isPriceIncrease = newPrice.compareTo(oldPrice) > 0;

        // Send email notification to each user who has this product in their wishlist
        for (RealWishlist item : wishlistItems) {
            try {
                sendPriceChangeEmail(item.getUser(), product, oldPrice, newPrice, priceChangePercentage, isPriceIncrease);
            } catch (Exception e) {
                // Log the error but continue with other notifications
                System.err.println("Failed to send price change notification to user: " + item.getUser().getEmail());
                e.printStackTrace();
            }
        }
    }

    /**
     * Calculates the percentage change between old and new price
     */
    private BigDecimal calculatePriceChangePercentage(BigDecimal oldPrice, BigDecimal newPrice) {
        if (oldPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(100); // If old price was 0, consider it 100% change
        }

        return newPrice.subtract(oldPrice)
                .divide(oldPrice, 2, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .abs(); // We'll handle increase/decrease separately
    }

    /**
     * Sends an email notification about price change to a user
     */
    private void sendPriceChangeEmail(User user, Product product, BigDecimal oldPrice, BigDecimal newPrice,
                                      BigDecimal changePercentage, boolean isPriceIncrease) throws Exception {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        String changeDirection = isPriceIncrease ? "increased" : "decreased";
        String changeColor = isPriceIncrease ? "#ff4c4c" : "#4caf50";
        String arrowIcon = isPriceIncrease ? "↑" : "↓";

        // Set email recipient and subject
        helper.setTo(user.getEmail());
        helper.setSubject("Price Change Alert for " + product.getName() + " - " + companyName);

        // Create HTML email template with professional design similar to OrderService
        String emailTemplate = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }
                .container { max-width: 600px; margin: 0 auto; }
                .header { background-color: #DE2B8E; color: white; padding: 20px; text-align: center; }
                .content { padding: 20px; background-color: #f9f9f9; border: 1px solid #FFC0DA; }
                .footer { padding: 15px; text-align: center; font-size: 0.8em; color: #777; }
                .highlight { color: #333; font-weight: bold; }
                .contact-info { margin-top: 20px; background-color: #fff; padding: 10px; border: 1px solid #FFC0DA; }
                .price-change { background-color: #FFF0F7; padding: 15px; margin: 15px 0; border-left: 3px solid #DE2B8E; }
                .price-tag { font-size: 18px; font-weight: bold; }
                .old-price { text-decoration: line-through; color: #888; }
                .new-price { color: %s; font-weight: bold; }
                .change-percent { display: inline-block; background-color: %s; color: white; padding: 2px 8px; border-radius: 12px; font-size: 14px; margin-left: 10px; }
                .product-info { display: flex; align-items: center; margin: 15px 0; }
                .product-image { width: 100px; height: 100px; background-color: #eee; text-align: center; line-height: 100px; margin-right: 15px; }
                .product-details { flex: 1; }
                .cta-button { display: inline-block; background-color: #DE2B8E; color: black; padding: 10px 20px; text-decoration: none; border-radius: 4px; margin-top: 15px; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>Price Change Alert!</h1>
                </div>
                <div class="content">
                    <p>Hello <span class="highlight">%s</span>,</p>
                    
                    <p>We wanted to let you know that a product in your wishlist has had a price change.</p>
                    
                    <div class="product-info">
                        <div class="product-image">
                            %s
                        </div>
                        <div class="product-details">
                            <h3>%s</h3>
                            <p>The price has <strong>%s</strong> by <span class="change-percent">%s%% %s</span></p>
                            <p>
                                <span class="price-tag old-price">%.2f ₺</span> → 
                                <span class="price-tag new-price">%.2f ₺</span>
                            </p>
                        </div>
                    </div>
                    
                    <div class="price-change">
                        <p>Don't miss out! The price change occurred on <span class="highlight">%s</span>.</p>
                        <p>This might be a good time to %s this item from your wishlist.</p>
                        <a href="http://localhost:3000/product/%d" class="cta-button">View Product</a>
                    </div>
                    
                    <p>Thank you for shopping with us at <span class="highlight">%s</span>!</p>
                    
                    <div class="contact-info">
                        <h4>Customer Support</h4>
                        <p>Email: %s<br>
                        Phone: %s<br>
                        Hours: Monday-Friday, 9:00 AM to 6:00 PM</p>
                    </div>
                </div>
                <div class="footer">
                    <p>&copy; %d %s. All rights reserved.<br>
                    %s</p>
                </div>
            </div>
        </body>
        </html>
        """;

        // Format the product image placeholder or actual image if available
        String productImageHtml = "📸";
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            productImageHtml = "<img src='" + product.getImageUrl() + "' alt='" + product.getName() + "' style='max-width: 100px; max-height: 100px;'>";
        }

        // Suggestion based on price change
        String suggestion = isPriceIncrease ? "consider alternatives to" : "purchase";

        String formattedEmail = String.format(emailTemplate,
                changeColor,
                changeColor,
                user.getFullName(),
                productImageHtml,
                product.getName(),
                changeDirection,
                changePercentage.setScale(1, BigDecimal.ROUND_HALF_UP),
                arrowIcon,
                oldPrice,
                newPrice,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")),
                suggestion,
                product.getId(),
                companyName,
                companyEmail,
                companyPhone,
                LocalDateTime.now().getYear(),
                companyName,
                companyAddress);

        // Set the HTML content
        helper.setText(formattedEmail, true);

        // Send the email
        javaMailSender.send(message);
    }
}