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

    // Tüm ürünleri getir
    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    // Ürün detayını getir ve görüntülenme sayısını artır
    @Transactional
    public Product getProductById(Long id) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Ürün görüntülendiğinde görüntülenme sayısını artır
        product.incrementViewCount();
        productRepo.save(product);

        return product;
    }

    // Sepete eklenme sayısına göre en popüler ürünler
    public List<Product> getMostWishlistedProducts() {
        return productRepo.findAllByOrderByWishlistCountDesc();
    }

    // Tıklanma sayısına göre en popüler ürünler
    public List<Product> getMostViewedProducts() {
        return productRepo.findAllByOrderByViewCountDesc();
    }

    // Hem sepet hem tıklanma sayısına göre kombine popülerlik
    public List<Product> getMostPopularProducts() {
        List<Product> allProducts = productRepo.findAll();

        // Her ürün için popülerlik skorunu hesapla (0.6 * wishlistCount + 0.4 * viewCount)
        for (Product product : allProducts) {
            product.calculatePopularityScore();
        }

        // Popülerlik skoruna göre sırala (yüksekten düşüğe)
        return allProducts.stream()
                .sorted(Comparator.comparing(Product::getPopularityScore).reversed())
                .collect(Collectors.toList());
    }
}