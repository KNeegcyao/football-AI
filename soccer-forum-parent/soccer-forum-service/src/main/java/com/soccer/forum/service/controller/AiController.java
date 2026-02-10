package com.soccer.forum.service.controller;

import com.soccer.forum.service.service.FootballAiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final FootballAiService aiService;

    public AiController(FootballAiService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/chat")
    public Map<String, String> chat(@RequestParam String message) {
        String response = aiService.chat(message);
        return Map.of("response", response);
    }
}
