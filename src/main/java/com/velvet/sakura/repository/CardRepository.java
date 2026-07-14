package com.velvet.sakura.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.velvet.sakura.entity.Card;
import com.velvet.sakura.entity.DeckType;

import java.util.List;


public interface CardRepository extends JpaRepository<Card,Long>{
List<Card> findByDeckType(DeckType deckType);
    
}