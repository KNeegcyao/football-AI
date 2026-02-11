package com.soccer.forum.service.controller;

import com.soccer.forum.service.service.FootballAiService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@Tag(name = "AI助手", description = "AI智能助手接口")
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
