package cs308.backhend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Data
@Table(name = "products")
@JsonIgnoreProperties({"categories"})
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String model;

    @Column(unique = true)
    private String serialNumber;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private int quantityInStock;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private boolean warrantyStatus = false;

    @Column(columnDefinition = "TEXT")
    private String distributorInfo;

    @ManyToMany
    @JoinTable(
            name = "product_categories",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @JsonIgnoreProperties("products")
    private Set<Category> categories;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String imageUrl;
}