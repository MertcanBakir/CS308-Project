package cs308.backhend.repository;

import cs308.backhend.model.Card;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardRepo  extends JpaRepository<Card, Long> {
    List<Card> findByUserId(Long id);


}
