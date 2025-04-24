package cs308.backhend;

import cs308.backhend.model.Product;
import cs308.backhend.repository.ProductRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ProductRepoDatabaseTest {

    @Autowired
    private ProductRepo productRepo;

    @Test
    @DisplayName("should sort products by price descending")
    void shouldSortProductsByPriceDescending() {
        Product p1 = new Product();
        p1.setName("Cheap");
        p1.setPrice(new BigDecimal("10.00"));
        p1.setQuantityInStock(10);

        Product p2 = new Product();
        p2.setName("Mid");
        p2.setPrice(new BigDecimal("50.00"));
        p2.setQuantityInStock(10);

        Product p3 = new Product();
        p3.setName("Expensive");
        p3.setPrice(new BigDecimal("100.00"));
        p3.setQuantityInStock(10);

        productRepo.saveAll(List.of(p1, p2, p3));

        var sorted = productRepo.findAll()
                .stream()
                .sorted((a, b) -> b.getPrice().compareTo(a.getPrice()))
                .toList();

        assertThat(sorted).extracting(Product::getName)
                .containsExactly("Expensive", "Mid", "Cheap");
    }
}
