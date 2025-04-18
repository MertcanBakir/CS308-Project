package cs308.backhend.repository;

import cs308.backhend.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByProduct_IdAndApprovedTrue(Long productId);
    List<Comment> findByApprovedFalse();
    List<Comment> findByProduct_Id(Long productId);

}
