package cs308.backhend.service;

import cs308.backhend.model.Product;
import cs308.backhend.repository.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepo productRepo;

    @Autowired
    public ProductService(ProductRepo productRepo) {
        this.productRepo = productRepo;
    }

    // Tüm ürünleri getir
    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }
}