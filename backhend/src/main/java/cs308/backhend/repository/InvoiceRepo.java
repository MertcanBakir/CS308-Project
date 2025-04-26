package cs308.backhend.repository;

import cs308.backhend.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepo extends JpaRepository<Invoice, Long> {
    Invoice findByOrders_Id(Long orderId);

}