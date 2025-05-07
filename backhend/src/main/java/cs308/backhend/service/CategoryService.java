package cs308.backhend.service;

import cs308.backhend.model.Category;
import cs308.backhend.repository.CategoryRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    private final CategoryRepo categoryRepo;

    public CategoryService(CategoryRepo categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    public List<Category> getAllCategories() {
        return categoryRepo.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepo.findById(id).orElse(null);
    }

    public Category addCategory(Category category) {
        return categoryRepo.save(category);
    }

    @Transactional
    public boolean deleteCategory(Long id) {
        Optional<Category> categoryOptional = categoryRepo.findById(id);
        if (categoryOptional.isPresent()) {
            Category category = categoryOptional.get();

            if (category.getProducts() != null && !category.getProducts().isEmpty()) {
                category.getProducts().forEach(product -> product.getCategories().remove(category));
            }

            categoryRepo.deleteById(id);
            return true;
        }
        return false;
    }

    public Category updateCategory(Long id, Category categoryDetails) {
        Optional<Category> categoryOptional = categoryRepo.findById(id);
        if (categoryOptional.isPresent()) {
            Category existingCategory = categoryOptional.get();
            existingCategory.setName(categoryDetails.getName());
            return categoryRepo.save(existingCategory);
        }
        return null;
    }
}