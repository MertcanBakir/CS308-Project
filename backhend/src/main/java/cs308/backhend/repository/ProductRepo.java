package cs308.backhend.repository;

import cs308.backhend.model.Product;
import cs308.backhend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepo extends JpaRepository<Product, Long>{

    Optional<Product> findByName(String name);

    // Belirli bir kategoriye ait ürünleri getir
    List<Product> findByCategories(Category category);

    // Stok miktarı 0 olan ürünleri getir (Tükendi)
    List<Product> findByQuantityInStock(int quantity);

    List<Product> findByid(Long id);

    List<Product> findByCategories_Id(Long categoryId);


}
