package cs308.backhend.repository;

import cs308.backhend.model.RealWishlist;
import cs308.backhend.model.User;
import cs308.backhend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RealWishlistRepo extends JpaRepository<RealWishlist, Long> {
    List<RealWishlist> findByUser(User user);
    Optional<RealWishlist> findByUserAndProduct(User user, Product product);
}
