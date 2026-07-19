package com.velvet.sakura.controller;

import com.velvet.sakura.dto.request.GenerateInterpretationRequest;
import com.velvet.sakura.dto.response.InterpretationResponse;
import com.velvet.sakura.exception.ResourceNotFoundException;
import com.velvet.sakura.entity.Card;
import com.velvet.sakura.repository.CardRepository;
import com.velvet.sakura.service.OpenAIService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interpretation")
@RequiredArgsConstructor
public class InterpretationController {

    private final OpenAIService openAIService;
    private final CardRepository cardRepository;

    @PostMapping
    public InterpretationResponse generate(@Valid @RequestBody GenerateInterpretationRequest request) {
        Card past = cardRepository.findById(request.getPastCardId())
                .orElseThrow(() -> new ResourceNotFoundException("Carta de pasado no encontrada"));
        Card present = cardRepository.findById(request.getPresentCardId())
                .orElseThrow(() -> new ResourceNotFoundException("Carta de presente no encontrada"));
        Card future = cardRepository.findById(request.getFutureCardId())
                .orElseThrow(() -> new ResourceNotFoundException("Carta de futuro no encontrada"));

        String interpretation = openAIService.generateInterpretation(
                request.getQuestion(),
                past.getSpanishName(), past.getMeaning(),
                present.getSpanishName(), present.getMeaning(),
                future.getSpanishName(), future.getMeaning()
        );

        return new InterpretationResponse(interpretation);
    }
}