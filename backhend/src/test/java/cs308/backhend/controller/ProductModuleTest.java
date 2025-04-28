package cs308.backhend;

import cs308.backhend.model.Product;
import cs308.backhend.repository.ProductRepo;
import cs308.backhend.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    @Test
    @DisplayName("should not allow saving products with negative stock")
    void shouldNotAllowNegativeStock() {
        Product product = new Product();
        product.setName("Faulty Product");
        product.setPrice(new BigDecimal("25.00"));
        product.setQuantityInStock(-5);

        productRepo.save(product);

        List<Product> products = productRepo.findAll();

        // Hiçbir ürün stok adedi negatif olmamalı!
        assertThat(products)
                .allMatch(p -> p.getQuantityInStock() >= 0, "Stock must be non-negative");
    }

    @WebMvcTest(cs308.backhend.controller.ProductController.class)
    @AutoConfigureMockMvc(addFilters = false)
    @Import(ProductRepoDatabaseTest.ProductControllerTestConfig.class)
    static class ProductSearchTest {

        @Autowired private MockMvc mockMvc;
        @Autowired private ProductService productService;

        @Test
        @DisplayName("Ürün araması başarılı şekilde sonuç dönmeli")
        void testProductSearch_ReturnsCorrectResults() throws Exception {
            Product mascara = new Product();
            mascara.setId(1L);
            mascara.setName("Volume Mascara");

            when(productService.searchProducts("Mascara")).thenReturn(List.of(mascara));

            mockMvc.perform(get("/products/search")
                            .param("query", "Mascara")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("Volume Mascara"));
        }

        @TestConfiguration
        static class ProductControllerTestConfig {
            @Bean public ProductService productService() {
                return Mockito.mock(ProductService.class);
            }
        }
    }
}
