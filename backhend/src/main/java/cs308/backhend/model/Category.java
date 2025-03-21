package cs308.backhend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.util.Set;


@Entity
@Data
@Table(name = "categories")
@JsonIgnoreProperties({"products"})
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "categories")
    @JsonIgnoreProperties("categories") 
    private Set<Product> products;
}