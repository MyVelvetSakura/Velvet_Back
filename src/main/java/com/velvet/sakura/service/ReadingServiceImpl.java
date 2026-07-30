package com.velvet.sakura.service;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.velvet.sakura.dto.request.CreateReadingRequest;
import com.velvet.sakura.dto.response.ReadingResponse;
import com.velvet.sakura.entity.Reading;
import com.velvet.sakura.repository.ReadingRepository;
import com.velvet.sakura.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReadingServiceImpl implements ReadingService {

    private final ReadingRepository readingRepository;
    private final ProgressService progressService;

    @Override
    public ReadingResponse createReading(CreateReadingRequest request) {
        Reading reading = Reading.builder()
                .userId(request.getUserId())
                .date(LocalDateTime.now())
                .name(request.getName())
                .pastCardId(request.getPastCardId())
                .presentCardId(request.getPresentCardId())
                .futureCardId(request.getFutureCardId())
                .deckType(request.getDeckType())
                .question(request.getQuestion())
                .interpretation(request.getInterpretation())
                .build();

        Reading saved = readingRepository.save(reading);

        progressService.registerReadingCompleted(
                request.getUserId(),
                request.getDeckType().toString(),
                request.getPastCardId(),
                request.getPresentCardId(),
                request.getFutureCardId());

        return toResponse(saved);
    }

    @Override
    public List<ReadingResponse> findByUserId(Long userId) {
        return readingRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ReadingResponse updateName(Long id, String newName) {
        Reading reading = readingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lectura no encontrada con id " + id));
        reading.setName(newName);
        return toResponse(readingRepository.save(reading));
    }

    @Override
    public void deleteReading(Long id) {
        if (!readingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Lectura no encontrada con id " + id);
        }
        readingRepository.deleteById(id);
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        List<Reading> readings = readingRepository.findByUserId(userId);
        readingRepository.deleteAll(readings);
    }

    private ReadingResponse toResponse(Reading reading) {
        return new ReadingResponse(
                reading.getId(),
                reading.getUserId(),
                reading.getDate(),
                reading.getName(),
                reading.getPastCardId(),
                reading.getPresentCardId(),
                reading.getFutureCardId(),
                reading.getDeckType(),
                reading.getQuestion(),
                reading.getInterpretation());
    }

    @Override
    public ReadingResponse findById(Long id) {
        Reading reading = readingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lectura no encontrada"));
        return toResponse(reading);
    }

    @Override
    public Page<ReadingResponse> findByUserIdPaginated(Long userId, Pageable pageable) {
        return readingRepository.findByUserId(userId, pageable).map(this::toResponse);
    }
}
