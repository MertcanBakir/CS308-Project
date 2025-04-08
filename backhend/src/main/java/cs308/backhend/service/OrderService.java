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

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepo orderRepo;
    private final JavaMailSender javaMailSender;

    public void generateInvoiceAndSendEmail(Order order, User user) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, baos);
        document.open();

        Paragraph title = new Paragraph("INVOICE", new Font(Font.HELVETICA, 20, Font.BOLD));
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        document.add(new Paragraph("Customer: " + user.getFullName()));
        document.add(new Paragraph("Email: " + user.getEmail()));
        document.add(new Paragraph("Address: " + order.getAddress().getAddress()));
        document.add(new Paragraph("Order Date: " + order.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))));
        document.add(new Paragraph(" "));

        Table table = new Table(3);
        table.setWidth(100);
        table.addCell("Product");
        table.addCell("Qty");
        table.addCell("Total");

        table.addCell(order.getProduct().getName());
        table.addCell(String.valueOf(order.getQuantity()));
        table.addCell(order.getProduct().getPrice()
                .multiply(BigDecimal.valueOf(order.getQuantity())) + " ₺");

        document.add(table);
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
    Your order has been successfully placed. Please find the attached PDF invoice for your reference.</p>

    <ul>
        <li><strong>Product:</strong> %s</li>
        <li><strong>Quantity:</strong> %d</li>
        <li><strong>Total Amount:</strong> %.2f ₺</li>
    </ul>

    <p>If you have any questions, feel free to contact us.</p>
    <p>Best regards,<br>
    <strong>Sephora Store Team 19</strong></p>
""", user.getFullName(), order.getProduct().getName(), order.getQuantity(),
                order.getProduct().getPrice().multiply(BigDecimal.valueOf(order.getQuantity()))), true);

        DataSource dataSource = new ByteArrayDataSource(baos.toByteArray(), "application/pdf");
        helper.addAttachment("invoice.pdf", dataSource);

        javaMailSender.send(message);
    }
}