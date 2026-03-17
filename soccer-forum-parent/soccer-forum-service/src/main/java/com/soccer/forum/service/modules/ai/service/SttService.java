package com.soccer.forum.service.modules.ai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class SttService {

    private static final Logger log = LoggerFactory.getLogger(SttService.class);

    @Value("${langchain4j.zhipu-ai.chat-model.api-key}")
    private String zhipuApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String ZHIPU_STT_URL = "https://open.bigmodel.cn/api/paas/v4/audio/transcriptions";

    public String transcribe(MultipartFile file) {
        if (zhipuApiKey == null || zhipuApiKey.isEmpty() || "your-key-here".equals(zhipuApiKey)) {
            log.warn("智谱 AI API Key 未配置，返回模拟识别结果");
            return "（模拟识别结果）曼城明天的比赛是什么时间？";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(zhipuApiKey);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", file.getResource());
            body.add("model", "cogvoice-7b"); // 智谱语音识别模型，或者根据最新文档调整

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(ZHIPU_STT_URL, requestEntity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map result = response.getBody();
                return (String) result.get("text");
            }
        } catch (Exception e) {
            log.error("语音识别异常: {}", e.getMessage(), e);
        }

        return "（识别失败，请检查配置）";
    }
}
