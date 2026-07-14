package com.velvet.sakura.service;

import java.util.List;

import com.velvet.sakura.dto.response.CardResponse;
import com.velvet.sakura.entity.DeckType;

public interface CardService {
    List<CardResponse> findByDeckType(DeckType deckType);
    CardResponse findById(Long id);
}
