package cs308.backhend.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.activation.DataSource;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import cs308.backhend.model.User;
import cs308.backhend.model.Order;
import cs308.backhend.repository.OrderRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List; // ✅ Doğru olan import bu

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepo orderRepo;
    private final JavaMailSender javaMailSender;

    public void generateInvoiceAndSendEmail(List<Order> orders, User user) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, baos);
        document.open();
        Image logo = Image.getInstance("src/main/resources/static/logo.png");
        logo.scaleToFit(100, 100);
        logo.setAlignment(Image.ALIGN_LEFT);
        document.add(logo);

        Paragraph title = new Paragraph("INVOICE", new Font(Font.HELVETICA, 20, Font.BOLD));
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        document.add(new Paragraph("Customer: " + user.getFullName()));
        document.add(new Paragraph("Email: " + user.getEmail()));
        document.add(new Paragraph("Order Count: " + orders.size()));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Date: " + orders.get(0).getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))));

        Table table = new Table(3);
        table.setWidth(100);
        table.addCell("Product");
        table.addCell("Qty");
        table.addCell("Total");

        BigDecimal grandTotal = BigDecimal.ZERO;

        for (Order order : orders) {
            table.addCell(order.getProduct().getName());
            table.addCell(String.valueOf(order.getQuantity()));
            BigDecimal total = order.getProduct().getPrice().multiply(BigDecimal.valueOf(order.getQuantity()));
            table.addCell(total + " ₺");
            grandTotal = grandTotal.add(total);
        }

        document.add(table);
        document.add(new Paragraph("Total Amount: " + grandTotal + " ₺"));
        document.add(new Paragraph(" "));

        Paragraph thanks = new Paragraph("Thank you for shopping with us!", new Font(Font.HELVETICA, 12));
        thanks.setAlignment(Element.ALIGN_CENTER);
        document.add(thanks);

        document.close();

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(user.getEmail());
        helper.setSubject("Your Order Invoice");

        helper.setText(String.format("""
            <p>Hello %s,</p>
            <p>Thank you for your purchase! 🛍️<br>
            Your orders have been placed successfully. Please find the attached PDF invoice for your reference.</p>
            <p>Number of Orders: %d<br>
            Total Amount: %.2f ₺</p>
            <p>Best regards,<br><strong>Sephora Store Team 19</strong></p>
        """, user.getFullName(), orders.size(), grandTotal), true);

        DataSource dataSource = new ByteArrayDataSource(baos.toByteArray(), "application/pdf");
        helper.addAttachment("invoice.pdf", dataSource);
        javaMailSender.send(message);
    }
}