package com.velvet.sakura.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.velvet.sakura.dto.response.CardResponse;
import com.velvet.sakura.entity.DeckType;
import com.velvet.sakura.service.CardService;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping
    public List<CardResponse> getDeck(@RequestParam(defaultValue = "SAKURA") DeckType deckType) {
        return cardService.findByDeckType(deckType);
    }

    @GetMapping("/{id}")
    public CardResponse getById(@PathVariable Long id) {
        return cardService.findById(id);
    }
}