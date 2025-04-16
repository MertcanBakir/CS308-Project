package cs308.backhend.repository;

import cs308.backhend.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepo extends JpaRepository<Order, Long> {

    List<Order> findByUser_Id(Long userId);

    boolean existsByUser_IdAndProduct_Id(Long userId, Long productId);

    @Query("SELECT COUNT(o) > 0 FROM Order o WHERE o.user.id = :userId AND o.product.id = :productId")
    boolean hasUserBoughtProduct(@Param("userId") Long userId, @Param("productId") Long productId);
}
