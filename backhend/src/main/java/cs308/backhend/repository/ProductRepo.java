package cs308.backhend.repository;

import cs308.backhend.model.Product;
import cs308.backhend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepo extends JpaRepository<Product, Long>{

    Optional<Product> findByName(String name);


    List<Product> findByCategories(Category category);


    List<Product> findByQuantityInStock(int quantity);

    List<Product> findByid(Long id);

    List<Product> findByCategories_Id(Long categoryId);

    List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);


    List<Product> findAllByOrderByWishlistCountDesc();


    List<Product> findAllByOrderByViewCountDesc();


    @Query("SELECT p FROM Product p ORDER BY (p.wishlistCount * 0.6 + p.viewCount * 0.4) DESC")
    List<Product> findByPopularity();

}
