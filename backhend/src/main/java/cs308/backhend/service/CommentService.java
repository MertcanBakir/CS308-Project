package cs308.backhend.service;

import cs308.backhend.model.Comment;
import cs308.backhend.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    public Comment addComment(Comment comment) {
        comment.setApproved(true);
        return commentRepository.save(comment);
    }

    public List<Comment> getApprovedCommentsByProductId(Long productId) {
        return commentRepository.findByProduct_IdAndApprovedTrue(productId);
    }

    public Optional<Comment> getCommentById(Long id) {
        return commentRepository.findById(id);
    }

    public void approveComment(Long id) {
        Optional<Comment> commentOptional = commentRepository.findById(id);
        if (commentOptional.isPresent()) {
            Comment comment = commentOptional.get();
            comment.setApproved(true);
            commentRepository.save(comment);
        }
    }

    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }
}
