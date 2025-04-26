package cs308.backhend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@ToString(exclude = "pdfData")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String invoiceNumber;

    private LocalDateTime createdAt;

    @JsonIgnore
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] pdfData;

    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL)
    private List<Order> orders;
}