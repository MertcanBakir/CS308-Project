package cs308.backhend.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import cs308.backhend.model.*;
import jakarta.activation.DataSource;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import cs308.backhend.repository.InvoiceRepo;
import cs308.backhend.repository.OrderRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    @Autowired
    private final OrderRepo orderRepo;
    @Autowired
    private final JavaMailSender javaMailSender;
    @Autowired
    private final InvoiceRepo invoiceRepository;


    private final Color sephoraPink = new Color(222, 43, 142);
    private final Color sephoraLightPink = new Color(255, 192, 218);
    private final Color sephoraDarkGrey = new Color(51, 51, 51);

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

    public void generateInvoiceAndSendEmail(List<Order> orders, User user) throws Exception {
        if (orders == null || orders.isEmpty()) {
            throw new IllegalArgumentException("Orders list cannot be empty");
        }

        Order firstOrder = orders.get(0);
        Address deliveryAddress = firstOrder.getAddress();
        Card paymentCard = firstOrder.getCard();

        String invoiceNumber = generateInvoiceNumber();
        LocalDateTime invoiceDate = LocalDateTime.now();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);
        document.open();

        // Add styling - Updated with Sephora colors
        Font titleFont = new Font(Font.HELVETICA, 22, Font.BOLD, sephoraPink);
        Font headingFont = new Font(Font.HELVETICA, 14, Font.BOLD, sephoraPink);
        Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL);
        Font smallFont = new Font(Font.HELVETICA, 8, Font.NORMAL);
        Font boldFont = new Font(Font.HELVETICA, 10, Font.BOLD);


        try {
            Image logo = Image.getInstance(this.getClass().getClassLoader().getResource("static/logo.png"));
            logo.scaleToFit(150, 150);
            logo.setAlignment(Image.ALIGN_LEFT);
            document.add(logo);
        } catch (Exception e) {

            Paragraph companyNameText = new Paragraph(companyName, titleFont);
            document.add(companyNameText);
        }

        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1, 1});
        headerTable.setSpacingBefore(10);
        headerTable.setSpacingAfter(20);


        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.addElement(new Paragraph(companyName, boldFont));
        leftCell.addElement(new Paragraph(companyAddress, normalFont));
        leftCell.addElement(new Paragraph("Tel: " + companyPhone, normalFont));
        leftCell.addElement(new Paragraph("Email: " + companyEmail, normalFont));
        leftCell.addElement(new Paragraph("Web: " + companyWebsite, normalFont));
        headerTable.addCell(leftCell);


        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph invoiceTitle = new Paragraph("INVOICE", titleFont);
        invoiceTitle.setAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(invoiceTitle);

        Paragraph invoiceNumberPara = new Paragraph("Invoice #: " + invoiceNumber, boldFont);
        invoiceNumberPara.setAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(invoiceNumberPara);

        Paragraph datePara = new Paragraph("Date: " + invoiceDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")), normalFont);
        datePara.setAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(datePara);

        headerTable.addCell(rightCell);
        document.add(headerTable);


        LineSeparator ls = new LineSeparator();
        ls.setLineColor(sephoraLightPink);
        ls.setLineWidth(2f);
        document.add(ls);
        document.add(new Paragraph(" ", normalFont));


        Paragraph customerTitle = new Paragraph("BILL TO:", headingFont);
        document.add(customerTitle);

        Paragraph customerInfo = new Paragraph();
        customerInfo.add(new Chunk(user.getFullName() + "\n", boldFont));
        customerInfo.add(new Chunk("Email: " + user.getEmail(), normalFont));
        customerInfo.setSpacingAfter(10);
        document.add(customerInfo);


        Paragraph paymentTitle = new Paragraph("PAYMENT METHOD:", headingFont);
        document.add(paymentTitle);


        PdfPTable paymentTable = new PdfPTable(2);
        paymentTable.setWidths(new float[]{0.3f, 5.7f});
        paymentTable.setWidthPercentage(100);
        paymentTable.setSpacingBefore(10);

        try {
            Image cardIcon = Image.getInstance(getClass().getResource("/static/card-icon.png"));
            cardIcon.scaleToFit(20, 20);
            PdfPCell iconCell = new PdfPCell(cardIcon, false);
            iconCell.setBorder(Rectangle.NO_BORDER);
            iconCell.setPaddingRight(1f);
            iconCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            iconCell.setFixedHeight(16);
            paymentTable.addCell(iconCell);
        } catch (Exception e) {
            PdfPCell placeholder = new PdfPCell(new Phrase("💳"));
            placeholder.setBorder(Rectangle.NO_BORDER);
            placeholder.setHorizontalAlignment(Element.ALIGN_CENTER);
            placeholder.setVerticalAlignment(Element.ALIGN_MIDDLE);
            paymentTable.addCell(placeholder);
        }

        PdfPCell paymentInfo = new PdfPCell();
        paymentInfo.setBorder(Rectangle.NO_BORDER);
        paymentInfo.addElement(new Paragraph("Credit Card ending in " + paymentCard.getLast4Digits(), normalFont));
        paymentInfo.addElement(new Paragraph("Cardholder: " + paymentCard.getName() + " " + paymentCard.getSurname(), normalFont));
        paymentInfo.addElement(new Paragraph("Expiry Date: " + paymentCard.getCardExpiryDate(), normalFont));
        paymentTable.addCell(paymentInfo);

        document.add(paymentTable);


        Paragraph addressTitle = new Paragraph("SHIPPING ADDRESS:", headingFont);
        document.add(addressTitle);


        PdfPTable shippingTable = new PdfPTable(2);
        shippingTable.setWidths(new float[]{0.3f, 5.7f});
        shippingTable.setWidthPercentage(100);
        shippingTable.setSpacingBefore(10);
        shippingTable.setSpacingAfter(10);

        try {
            Image addressIcon = Image.getInstance(getClass().getResource("/static/adres-icon.png"));
            addressIcon.scaleToFit(20, 20);
            PdfPCell iconCell = new PdfPCell(addressIcon, false);
            iconCell.setBorder(Rectangle.NO_BORDER);
            iconCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            iconCell.setFixedHeight(20);
            shippingTable.addCell(iconCell);
        } catch (Exception e) {
            PdfPCell placeholder = new PdfPCell(new Phrase("📍"));
            placeholder.setBorder(Rectangle.NO_BORDER);
            placeholder.setHorizontalAlignment(Element.ALIGN_CENTER);
            placeholder.setVerticalAlignment(Element.ALIGN_MIDDLE);
            shippingTable.addCell(placeholder);
        }

        PdfPCell shippingInfo = new PdfPCell(new Phrase(deliveryAddress.getAddress(), normalFont));
        shippingInfo.setBorder(Rectangle.NO_BORDER);
        shippingInfo.setVerticalAlignment(Element.ALIGN_MIDDLE); // 🔧 Bu olmazsa aşağı kayıyor
        shippingInfo.setFixedHeight(20); // 🔧 İkonla aynı hizaya gelmesi için
        shippingTable.addCell(shippingInfo);

        document.add(shippingTable);




        // Order Details Table
        document.add(new Paragraph("ORDER DETAILS:", headingFont));
        document.add(new Paragraph(" ", smallFont));

        PdfPTable orderTable = new PdfPTable(5);
        orderTable.setWidthPercentage(100);
        try {
            orderTable.setWidths(new float[]{1.5f, 3.5f, 1f, 1.5f, 1.5f});
        } catch (DocumentException e) {
            throw new RuntimeException("Error setting table widths", e);
        }

        // Table headers with updated Sephora pink color
        PdfPCell headerCell = new PdfPCell();
        headerCell.setBackgroundColor(sephoraPink);
        headerCell.setPadding(5);

        Font headerTextFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);

        headerCell.setPhrase(new Phrase("Item #", headerTextFont));
        orderTable.addCell(headerCell);

        headerCell.setPhrase(new Phrase("Product Description", headerTextFont));
        orderTable.addCell(headerCell);

        headerCell.setPhrase(new Phrase("Quantity", headerTextFont));
        orderTable.addCell(headerCell);

        headerCell.setPhrase(new Phrase("Unit Price", headerTextFont));
        orderTable.addCell(headerCell);

        headerCell.setPhrase(new Phrase("Amount", headerTextFont));
        orderTable.addCell(headerCell);


        PdfPCell cellStyle = new PdfPCell();
        cellStyle.setPadding(5);
        cellStyle.setBorderColor(sephoraLightPink);

        BigDecimal total = BigDecimal.ZERO;
        int itemNumber = 1;

        for (Order order : orders) {
            BigDecimal lineAmount = order.getProduct().getPrice().multiply(BigDecimal.valueOf(order.getQuantity()));
            total = total.add(lineAmount);

            cellStyle.setPhrase(new Phrase(String.valueOf(itemNumber++), normalFont));
            orderTable.addCell(cellStyle);

            cellStyle.setPhrase(new Phrase(order.getProduct().getName(), normalFont));
            orderTable.addCell(cellStyle);

            cellStyle.setPhrase(new Phrase(String.valueOf(order.getQuantity()), normalFont));
            orderTable.addCell(cellStyle);

            cellStyle.setPhrase(new Phrase(order.getProduct().getPrice() + " ₺", normalFont));
            orderTable.addCell(cellStyle);

            cellStyle.setPhrase(new Phrase(lineAmount + " ₺", normalFont));
            orderTable.addCell(cellStyle);
        }


        PdfPCell emptyCell = new PdfPCell(new Phrase(""));
        emptyCell.setColspan(5);
        emptyCell.setBorder(Rectangle.NO_BORDER);
        orderTable.addCell(emptyCell);


        PdfPCell totalLabelCell = new PdfPCell(new Phrase("TOTAL:", new Font(Font.HELVETICA, 12, Font.BOLD)));
        totalLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalLabelCell.setColspan(4);
        totalLabelCell.setBorder(Rectangle.NO_BORDER);
        orderTable.addCell(totalLabelCell);

        PdfPCell totalValueCell = new PdfPCell(new Phrase(total + " ₺", new Font(Font.HELVETICA, 12, Font.BOLD)));
        totalValueCell.setBorder(Rectangle.NO_BORDER);
        orderTable.addCell(totalValueCell);

        document.add(orderTable);


        document.add(new Paragraph(" ", normalFont));
        Paragraph statusInfo = new Paragraph("Order Status: " + firstOrder.getStatus().toString(), boldFont);
        statusInfo.setIndentationLeft(10);
        document.add(statusInfo);


        document.add(new Paragraph(" ", normalFont));
        Paragraph notesTitle = new Paragraph("Notes:", headingFont);
        document.add(notesTitle);

        Paragraph notes = new Paragraph("Order processed on " +
                firstOrder.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")) +
                ". Please keep this invoice for your records.", normalFont);
        notes.setIndentationLeft(10);
        document.add(notes);

        document.add(new Paragraph(" ", normalFont));
        Paragraph termsTitle = new Paragraph("Terms & Conditions:", headingFont);
        document.add(termsTitle);

        Paragraph terms = new Paragraph("Payment is due upon receipt. Returns accepted within 30 days with original packaging.", smallFont);
        terms.setIndentationLeft(10);
        document.add(terms);


        document.add(new Paragraph(" ", normalFont));
        LineSeparator footerLine = new LineSeparator();
        footerLine.setLineColor(sephoraPink);
        footerLine.setLineWidth(2f);
        document.add(footerLine);

        Paragraph thankYou = new Paragraph("Thank you for shopping with " + companyName + "!",
                new Font(Font.HELVETICA, 12, Font.BOLD, sephoraPink));
        thankYou.setAlignment(Element.ALIGN_CENTER);
        thankYou.setSpacingBefore(10);
        document.add(thankYou);

        document.close();
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setPdfData(baos.toByteArray());
        invoice.setUser(user);


        for (Order order : orders) {
            order.setInvoice(invoice);
        }

        invoice.setOrders(orders);
        invoiceRepository.save(invoice);

        sendInvoiceEmail(user, baos.toByteArray(), invoiceNumber, orders, total);
    }

    private void sendInvoiceEmail(User user, byte[] pdfBytes, String invoiceNumber, List<Order> orders, BigDecimal totalAmount) throws Exception {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(user.getEmail());
        helper.setSubject("Your Order Invoice #" + invoiceNumber + " - " + companyName);

        Order firstOrder = orders.get(0);
        Address deliveryAddress = firstOrder.getAddress();
        Card paymentCard = firstOrder.getCard();

        String emailTemplate = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; }
                    .header { background-color: #DE2B8E; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; border: 1px solid #FFC0DA; }
                    .footer { padding: 15px; text-align: center; font-size: 0.8em; color: #777; }
                    .highlight { color: #333; font-weight: bold; }
                    .contact-info { margin-top: 20px; background-color: #fff; padding: 10px; border: 1px solid #FFC0DA; }
                    .order-summary { background-color: #FFF0F7; padding: 15px; margin: 15px 0; border-left: 3px solid #DE2B8E; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Thank You For Your Order!</h1>
                    </div>
                    <div class="content">
                        <p>Hello <span class="highlight">%s</span>,</p>
                        
                        <p>Thank you for shopping with us at <span class="highlight">%s</span>! 🛍️</p>
                        
                        <p>We're pleased to confirm that your order has been processed successfully. Please find your invoice (#%s) attached to this email as a PDF file.</p>
                        
                        <div class="order-summary">
                            <h3>Order Summary:</h3>
                            <ul>
                                <li>Number of Items: <span class="highlight">%d</span></li>
                                <li>Order Date: <span class="highlight">%s</span></li>
                                <li>Total Amount: <span class="highlight">%.2f ₺</span></li>
                                <li>Shipping Address: <span class="highlight">%s</span></li>
                                <li>Payment Method: Credit Card ending in <span class="highlight">%s</span></li>
                            </ul>
                        </div>
                        
                        <p>Your items will be shipped to the address provided during checkout.</p>
                        
                        <p>If you have any questions about your order, please don't hesitate to contact our customer service team.</p>
                        
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

        String formattedEmail = String.format(emailTemplate,
                user.getFullName(),
                companyName,
                invoiceNumber,
                orders.size(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")),
                totalAmount,
                deliveryAddress.getAddress(),
                paymentCard.getLast4Digits(),
                companyEmail,
                companyPhone,
                LocalDateTime.now().getYear(),
                companyName,
                companyAddress);

        helper.setText(formattedEmail, true);

        DataSource dataSource = new ByteArrayDataSource(pdfBytes, "application/pdf");
        helper.addAttachment("Invoice_" + invoiceNumber + ".pdf", dataSource);

        javaMailSender.send(message);
    }

    private String generateInvoiceNumber() {
        // Format: Current Year + Month + Random Alphanumeric String
        LocalDateTime now = LocalDateTime.now();
        String yearMonth = now.format(DateTimeFormatter.ofPattern("yyMM"));
        String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "INV" + yearMonth + "-" + randomPart;
    }
}