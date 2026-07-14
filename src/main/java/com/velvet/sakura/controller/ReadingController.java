package com.velvet.sakura.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.velvet.sakura.dto.request.CreateReadingRequest;
import com.velvet.sakura.dto.response.ReadingResponse;
import com.velvet.sakura.service.ReadingService;

import java.util.List;

@RestController
@RequestMapping("/api/readings")
@RequiredArgsConstructor
public class ReadingController {

    private final ReadingService readingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReadingResponse create(@Valid @RequestBody CreateReadingRequest request) {
        return readingService.createReading(request);
    }

    @GetMapping(params = "userId")
    public List<ReadingResponse> getByUserId(@RequestParam Long userId) {
        return readingService.findByUserId(userId);
    }

    @PatchMapping("/{id}")
    public ReadingResponse updateName(@PathVariable Long id, @RequestBody String newName) {
        return readingService.updateName(id, newName);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        readingService.deleteReading(id);
    }

    @DeleteMapping(params = "userId")
    public void deleteAllByUser(@RequestParam Long userId) {
        readingService.deleteAllByUserId(userId);
    }
}
