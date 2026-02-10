package com.soccer.forum.service.service;

import dev.langchain4j.service.spring.AiService;

@AiService
public interface FootballAiService {
    String chat(String message);
}
