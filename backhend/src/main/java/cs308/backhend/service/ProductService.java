package cs308.backhend.service;

import cs308.backhend.model.Product;
import cs308.backhend.repository.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepo productRepo;

    @Autowired
    public ProductService(ProductRepo productRepo) {
        this.productRepo = productRepo;
    }

    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    public List<Product> getApprovedProducts() {
        return productRepo.findByApprovedTrue();
    }
    public void save(Product product) {
        productRepo.save(product);
    }

    public void deleteProductById(Long id) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        productRepo.delete(product);
    }



    public List<Product> searchProducts(String query) {
        return productRepo.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);
    }

    public List<Product> getProductsByCategoryId(Long categoryId) {
        return productRepo.findByCategories_Id(categoryId);
    }
    public Product addProduct(Product product) {
        return productRepo.save(product);
    }



    @Transactional
    public Product getProductById(Long id) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));


        product.incrementViewCount();
        productRepo.save(product);

        return product;
    }

    public void decrementStock(Product product, int change) {
        int stock = product.getQuantityInStock();
        stock = stock - change;
        if(stock < 0) {
            product.setQuantityInStock(0);

        }
        product.setQuantityInStock(stock);
    }

    public List<Product> getMostWishlistedProducts() {
        return productRepo.findAllByOrderByWishlistCountDesc();
    }


    public List<Product> getMostViewedProducts() {
        return productRepo.findAllByOrderByViewCountDesc();
    }


    public List<Product> getMostPopularProducts() {
        List<Product> allProducts = productRepo.findAll();


        for (Product product : allProducts) {
            product.calculatePopularityScore();
        }


        return allProducts.stream()
                .sorted(Comparator.comparing(Product::getPopularityScore).reversed())
                .collect(Collectors.toList());
    }
    @Transactional
    public Product updateProductNameAndStock(Long id, String newName, int newStock) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setName(newName);
        product.setQuantityInStock(newStock);

        return productRepo.save(product);
    }


}
