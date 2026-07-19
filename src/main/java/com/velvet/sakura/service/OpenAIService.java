package com.velvet.sakura.service;

public interface OpenAIService {
    String generateInterpretation(String question, String pastCard, String pastMeaning,
                                   String presentCard, String presentMeaning,
                                   String futureCard, String futureMeaning);
}
