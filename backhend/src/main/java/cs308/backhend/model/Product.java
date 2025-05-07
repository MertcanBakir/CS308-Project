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

    @Column(unique = true,nullable = false)
    private String serialNumber;

    @Column(columnDefinition = "TEXT",nullable = false)
    private String description;

    @Column(nullable = false)
    private int quantityInStock;

    @Column(nullable = true, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private boolean warrantyStatus;

    @Column(columnDefinition = "TEXT",nullable = false)
    private String distributorInfo;

    @ManyToMany
    @JoinTable(
            name = "product_categories",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @JsonIgnoreProperties("products")
    private Set<Category> categories;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String imageUrl;


    @Column(nullable = false)
    private int wishlistCount = 0;

    @Column(nullable = false)
    private int viewCount = 0;

    @Column(nullable = false)
    private boolean approved = false;



    @Transient
    private double popularityScore = 0.0;


    public void incrementWishlistCount() {
        this.wishlistCount++;
    }

    public void decrementWishlistCount() {
        if (this.wishlistCount > 0) {
            this.wishlistCount--;
        }
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public double calculatePopularityScore() {
        double viewWeight = 0.4;
        double wishlistWeight = 0.6;

        popularityScore = (viewCount * viewWeight) + (wishlistCount * wishlistWeight);
        return popularityScore;
    }

    public double getPopularityScore() {
        return this.popularityScore;
    }
}