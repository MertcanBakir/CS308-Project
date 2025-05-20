package cs308.backhend.service;

import cs308.backhend.model.Category;
import cs308.backhend.model.Product;
import cs308.backhend.repository.CategoryRepo;
import cs308.backhend.repository.ProductRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    private final CategoryRepo categoryRepo;
    private final ProductRepo productRepo;

    public CategoryService(CategoryRepo categoryRepo, ProductRepo productRepo) {
        this.categoryRepo = categoryRepo;
        this.productRepo = productRepo;

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

            List<Product> productsWithThisCategory = new ArrayList<>(productRepo.findByCategories_Id(id));
            for (Product product : productsWithThisCategory) {
                product.getCategories().remove(category);
                productRepo.save(product);
            }
            if (category.getProducts() != null) {
                category.getProducts().clear();
            }

            categoryRepo.delete(category);
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