package cs308.backhend.controller;

import cs308.backhend.model.Invoice;
import cs308.backhend.model.User;
import cs308.backhend.repository.InvoiceRepo;
import cs308.backhend.repository.UserRepo;
import cs308.backhend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping
public class InvoiceController {

    private final InvoiceRepo invoiceRepository;
    private final JwtUtil jwtUtil;
    private final UserRepo userRepo;

    @Autowired
    public InvoiceController(InvoiceRepo invoiceRepository, JwtUtil jwtUtil, UserRepo userRepo) {
        this.invoiceRepository = invoiceRepository;
        this.jwtUtil = jwtUtil;
        this.userRepo = userRepo;
    }

    @Transactional(readOnly = true)
    @GetMapping("/invoices/{orderId}/download")
    public ResponseEntity<?> downloadInvoice(@PathVariable Long orderId, @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            String userEmail = jwtUtil.extractEmail(jwt);

            User user = userRepo.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!jwtUtil.validateToken(jwt, userEmail)) {
                return ResponseEntity.status(401).body("Invalid or expired token!");
            }

            Invoice invoice = invoiceRepository.findByOrders_Id(orderId);
            if (invoice == null) {
                return ResponseEntity.status(404).body("Invoice not found for this order.");
            }

            if (!invoice.getUser().getId().equals(user.getId())) {
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
}