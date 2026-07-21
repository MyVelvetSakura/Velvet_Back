package com.velvet.sakura.service;


import java.util.List;

import com.velvet.sakura.dto.request.CreateReadingRequest;
import com.velvet.sakura.dto.response.ReadingResponse;

public interface ReadingService {
    ReadingResponse createReading(CreateReadingRequest request);
    List<ReadingResponse> findByUserId(Long userId);
    ReadingResponse updateName(Long id, String newName);
    void deleteReading(Long id);
    void deleteAllByUserId(Long userId);
    public ReadingResponse findById(Long id);
}
