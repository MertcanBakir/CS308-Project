package cs308.backhend.controller;

import cs308.backhend.model.Product;
import cs308.backhend.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ProductSearchTest.MockConfig.class)
public class ProductSearchTest {

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
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Volume Mascara"));
    }

    @TestConfiguration
    static class MockConfig {
        @Bean public ProductService productService() { return Mockito.mock(ProductService.class); }
    }
}
