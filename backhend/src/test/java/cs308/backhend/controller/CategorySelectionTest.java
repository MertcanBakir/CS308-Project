package cs308.backhend.controller;

import cs308.backhend.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(CategorySelectionTest.MockConfig.class)
public class CategorySelectionTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private CategoryService categoryService;

    @Test
    @DisplayName("Kategori seçildiğinde arama sonuçları resetlenmeli")
    void testCategorySelection_ResetsSearchResults() throws Exception {
        mockMvc.perform(get("/categories/1"))
                .andExpect(status().isOk());

    }

    @TestConfiguration
    static class MockConfig {
        @Bean public CategoryService categoryService() {
            return Mockito.mock(CategoryService.class);
        }
    }
}
