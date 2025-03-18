package cs308.backhend.repository;

import cs308.backhend.model.Wishlist;
import cs308.backhend.model.User;
import cs308.backhend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishListRepo extends JpaRepository<Wishlist, Long> {


    List<Wishlist> findByUser(User user);

    Optional<Wishlist> findByUserAndProduct(User user, Product product);

}