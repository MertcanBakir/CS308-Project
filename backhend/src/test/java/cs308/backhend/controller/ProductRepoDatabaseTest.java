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

    private Product createValidProduct(String name, BigDecimal price, int stock, String serialSuffix) {
        Product product = new Product();
        product.setName(name);
        product.setSerialNumber("SN-" + serialSuffix);
        product.setDescription("Test description for " + name);
        product.setQuantityInStock(stock);
        product.setPrice(price);
        product.setDistributorInfo("Test Distributor");
        product.setImageUrl("http://example.com/image_" + serialSuffix + ".jpg");
        product.setWarrantyStatus(true);
        product.setApproved(true);
        return product;
    }

    @Test
    @DisplayName("should sort products by price descending")
    void shouldSortProductsByPriceDescending() {
        Product p1 = createValidProduct("Cheap", new BigDecimal("10.00"), 10, "001");
        Product p2 = createValidProduct("Mid", new BigDecimal("50.00"), 10, "002");
        Product p3 = createValidProduct("Expensive", new BigDecimal("100.00"), 10, "003");

        productRepo.saveAll(List.of(p1, p2, p3));

        var sorted = productRepo.findAll()
                .stream()
                .sorted((a, b) -> b.getPrice().compareTo(a.getPrice()))
                .toList();

        assertThat(sorted).extracting(Product::getName)
                .containsExactly("Expensive", "Mid", "Cheap");
    }

    @Test
    @DisplayName("database allows negative stock - this should be handled at service layer")
    void databaseAllowsNegativeStockUnlessValidatedElsewhere() {
        Product product = createValidProduct("Faulty Product", new BigDecimal("25.00"), -5, "004");
        productRepo.save(product);

        Product saved = productRepo.findById(product.getId()).orElseThrow();
        assertThat(saved.getQuantityInStock()).isEqualTo(-5);
    }
}