package cs308.backhend;

import cs308.backhend.controller.ProductController;
import cs308.backhend.model.Product;
import cs308.backhend.service.ProductService;
import cs308.backhend.repository.UserRepo;
import cs308.backhend.security.JwtUtil;
import cs308.backhend.service.WishlistNotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProductControllerWebTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private ProductService productService;
    @MockBean private JwtUtil jwtUtil;
    @MockBean private UserRepo userRepo;
    @MockBean private WishlistNotificationService wishlistNotificationService;

    @Test
    @DisplayName("Ürün araması başarılı şekilde sonuç dönmeli")
    void testProductSearch_ReturnsCorrectResults() throws Exception {
        Product mascara = new Product();
        mascara.setId(1L);
        mascara.setName("Volume Mascara");
        mascara.setSerialNumber("SN-001");
        mascara.setDescription("A mascara that adds volume.");
        mascara.setQuantityInStock(20);
        mascara.setPrice(new BigDecimal("49.99"));
        mascara.setDistributorInfo("Test Distributor");
        mascara.setImageUrl("http://example.com/mascara.jpg");
        mascara.setWarrantyStatus(true);
        mascara.setApproved(true);

        when(productService.searchProducts("Mascara")).thenReturn(List.of(mascara));

        mockMvc.perform(get("/products/search")
                        .param("query", "Mascara")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Volume Mascara"));
    }
}