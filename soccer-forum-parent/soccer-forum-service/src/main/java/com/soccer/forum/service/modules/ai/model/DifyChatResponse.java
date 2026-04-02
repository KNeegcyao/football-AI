package com.soccer.forum.service.modules.ai.model;

import lombok.Data;

@Data
public class DifyChatResponse {
    private String event;
    private String task_id;
    private String message_id;
    private String conversation_id;
    private String mode;
    private String answer;
    private Long created_at;
}
