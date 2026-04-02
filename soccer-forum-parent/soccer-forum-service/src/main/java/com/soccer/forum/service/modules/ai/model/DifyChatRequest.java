package com.soccer.forum.service.modules.ai.model;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DifyChatRequest {
    private Map<String, Object> inputs;
    private String query;
    private String response_mode = "streaming";
    private String user;
    private String conversation_id;
    private List<Map<String, Object>> files;
}
