package cs308.backhend.controller;

import cs308.backhend.model.Category;
import cs308.backhend.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Transactional(readOnly = true)
    @GetMapping
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }
}