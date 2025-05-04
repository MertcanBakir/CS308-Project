package cs308.backhend.controller;
import cs308.backhend.dto.InvoiceResponse;
import cs308.backhend.model.Invoice;
import cs308.backhend.model.Order;
import cs308.backhend.model.User;
import cs308.backhend.repository.InvoiceRepo;
import cs308.backhend.repository.OrderRepo;
import cs308.backhend.repository.UserRepo;
import cs308.backhend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
@RestController
@RequestMapping
public class InvoiceController {

    private final InvoiceRepo invoiceRepository;
    private final JwtUtil jwtUtil;
    private final UserRepo userRepo;
    private final OrderRepo orderRepository;

    @Autowired
    public InvoiceController(
            InvoiceRepo invoiceRepository,
            JwtUtil jwtUtil,
            UserRepo userRepo,
            OrderRepo orderRepository) {
        this.invoiceRepository = invoiceRepository;
        this.jwtUtil = jwtUtil;
        this.userRepo = userRepo;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    @GetMapping("/invoices/{orderId}/download")
    public ResponseEntity<?> downloadInvoice(@PathVariable Long orderId, @RequestHeader("Authorization") String token) {
        try {
            if (!jwtUtil.validateToken(token, jwtUtil.extractEmail(token))) {
                return ResponseEntity.status(401).body("Invalid or expired token!");
            }

            String userEmail = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractRole(token);

            User user = userRepo.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Invoice invoice = invoiceRepository.findByOrders_Id(orderId);
            if (invoice == null) {
                return ResponseEntity.status(404).body("Invoice not found for this order.");
            }

            if (!invoice.getUser().getId().equals(user.getId()) && !role.equals("salesManager")) {
                return ResponseEntity.status(403).body("You are not authorized to access this invoice.");
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + invoice.getInvoiceNumber() + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(invoice.getPdfData());

        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/invoices")
    public ResponseEntity<List<InvoiceResponse>> getInvoicesByDateRange(
            @RequestHeader("Authorization") String token,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        try {
            if (!jwtUtil.validateToken(token, jwtUtil.extractEmail(token))) {
                return ResponseEntity.status(401).body(null);
            }

            String role = jwtUtil.extractRole(token);
            if (!"salesManager".equals(role)) {
                return ResponseEntity.status(403).body(null);
            }

            List<Order> orders = orderRepository.findByCreatedAtBetween(
                    start.atStartOfDay(), end.atTime(LocalTime.MAX));

            List<InvoiceResponse> result = orders.stream()
                    .map(order -> new InvoiceResponse(
                            order.getId(),
                            order.getUser().getFullName(),
                            order.getProduct().getName(),
                            order.getQuantity(),
                            order.getCreatedAt(),
                            order.getAddress().getAddress(),
                            order.getCard().getLast4Digits()
                    ))
                    .toList();

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
}