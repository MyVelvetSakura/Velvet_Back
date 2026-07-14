package com.velvet.sakura.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.velvet.sakura.dto.response.CardResponse;
import com.velvet.sakura.entity.Card;
import com.velvet.sakura.entity.DeckType;
import com.velvet.sakura.repository.CardRepository;
import com.velvet.sakura.exception.ResourceNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;

    @Override
    public List<CardResponse> findByDeckType(DeckType deckType) {
        return cardRepository.findByDeckType(deckType).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CardResponse findById(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carta no encontrada con id " + id));
        return toResponse(card);
    }

    private CardResponse toResponse(Card card) {
        return new CardResponse(
                card.getId(),
                card.getCode(),
                card.getSpanishName(),
                card.getMeaning(),
                card.getCardImageUrl(),
                card.getReverseImageUrl()
        );
    }
}