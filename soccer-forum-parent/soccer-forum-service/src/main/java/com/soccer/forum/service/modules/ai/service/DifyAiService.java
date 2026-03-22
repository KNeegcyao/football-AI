package com.soccer.forum.service.modules.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soccer.forum.service.modules.ai.model.DifyChatRequest;
import com.soccer.forum.service.modules.ai.model.DifyChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
public class DifyAiService {

    private static final Logger log = LoggerFactory.getLogger(DifyAiService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${dify.api.key}")
    private String difyApiKey;

    @Value("${dify.api.url}")
    private String difyApiUrl;

    private final RestTemplate restTemplate;

    public DifyAiService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000); // 10秒连接超时
        factory.setReadTimeout(120000);   // 120秒读取超时 (AI 分析图片可能很慢)
        this.restTemplate = new RestTemplate(factory);
    }

    public DifyChatResponse chat(String message, String conversationId, String userId, List<Map<String, Object>> files) {
        try {
            DifyChatRequest request = new DifyChatRequest();
            request.setInputs(new HashMap<>());
            request.setQuery(message != null && !message.trim().isEmpty() ? message : "分析这张图片");
            request.setUser(userId != null ? userId : "guest-unknown");
            // 只有当 conversationId 不为空且不为 "null" 时才设置
            if (conversationId != null && !conversationId.trim().isEmpty() && !"null".equals(conversationId)) {
                request.setConversation_id(conversationId);
            }
            request.setFiles(files);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.TEXT_EVENT_STREAM));
            headers.setBearerAuth(difyApiKey);

            HttpEntity<DifyChatRequest> entity = new HttpEntity<>(request, headers);

            log.info("转发请求到 Dify API (Streaming): {}, message: {}, user: {}, files: {}", difyApiUrl, message, request.getUser(), files);

            // 由于 Agent 模式仅支持 Streaming，我们需要手动聚合响应
            return restTemplate.execute(difyApiUrl, org.springframework.http.HttpMethod.POST,
                clientRequest -> {
                    clientRequest.getHeaders().addAll(headers);
                    objectMapper.writeValue(clientRequest.getBody(), request);
                },
                clientResponse -> {
                    log.info("收到 Dify 响应, 状态码: {}", clientResponse.getStatusCode());
                    StringBuilder fullAnswer = new StringBuilder();
                    String finalConversationId = conversationId;
                    String taskId = null;
                    
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientResponse.getBody(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String trimmedLine = line.trim();
                            if (trimmedLine.isEmpty()) continue;
                            
                            // 所有的 SSE 数据包都以 "data: " 开头
                            if (trimmedLine.startsWith("data:")) {
                                String jsonStr = trimmedLine.substring(trimmedLine.indexOf(":") + 1).trim();
                                
                                // 检查流结束标识
                                if ("[DONE]".equals(jsonStr)) {
                                    log.info("Dify SSE 流式传输正常结束");
                                    break;
                                }

                                try {
                                    JsonNode node = objectMapper.readTree(jsonStr);
                                    String event = node.path("event").asText();
                                    
                                    // 记录会话和任务 ID
                                    if (node.has("conversation_id")) finalConversationId = node.get("conversation_id").asText();
                                    if (node.has("task_id")) taskId = node.get("task_id").asText();

                                    // --- 核心内容提取策略 ---
                                    // 1. 优先提取标准 answer 字段 (适用于 message, agent_message, agent_thought 等多数事件)
                                    String answer = node.path("answer").asText();
                                    
                                    // 2. 兼容 text_chunk 或 delta 模式 (某些模型或流式配置下使用)
                                    if (answer == null || answer.isEmpty()) {
                                        answer = node.path("text").asText();
                                    }
                                    
                                    // 3. 兼容 delta 结构 (如 {"event": "text_chunk", "delta": {"text": "..."}})
                                    if ((answer == null || answer.isEmpty()) && node.has("delta")) {
                                        answer = node.path("delta").path("text").asText();
                                    }

                                    // 聚合有效内容
                                    if (answer != null && !answer.isEmpty()) {
                                        fullAnswer.append(answer);
                                    }

                                    // 处理错误事件
                                    if ("error".equals(event)) {
                                        String errMsg = node.path("message").asText();
                                        int errCode = node.path("code").asInt();
                                        log.error("Dify 转发过程中收到错误事件: [{}] {}", errCode, errMsg);
                                        // 如果已经有内容了，在后面追加错误提示；如果没有，则直接设为错误
                                        if (fullAnswer.length() > 0) fullAnswer.append("\n[中断: ").append(errMsg).append("]");
                                        else fullAnswer.append("Dify API 错误: ").append(errMsg);
                                    }
                                    
                                    // 打印调试信息（可选，为了不刷屏，仅在没拿到内容时记录关键事件）
                                    if (fullAnswer.length() == 0) {
                                        log.info("Dify 事件中: event={}, answer_present={}", event, (answer != null && !answer.isEmpty()));
                                    }

                                } catch (Exception e) {
                                    log.warn("无法解析 Dify SSE 数据行: {}, 错误: {}", trimmedLine, e.getMessage());
                                }
                            } else {
                                log.debug("忽略非 data 协议行: {}", trimmedLine);
                            }
                        }
                    }

                    DifyChatResponse response = new DifyChatResponse();
                    response.setAnswer(fullAnswer.toString());
                    response.setConversation_id(finalConversationId);
                    response.setTask_id(taskId);
                    
                    if (fullAnswer.length() == 0) {
                        log.warn("Dify API 未返回任何有效回复内容");
                        response.setAnswer("抱歉，AI 助手未返回任何内容。");
                    } else {
                        log.info("Dify API 聚合响应成功, conversationId: {}, taskId: {}", response.getConversation_id(), taskId);
                    }
                    return response;
                }
            );

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("Dify API 调用客户端异常 (HTTP {}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            
            // 如果是会话不存在，尝试清除会话 ID 并重新发起一次请求（不带会话 ID）
            String responseBody = e.getResponseBodyAsString();
            if (e.getStatusCode().value() == 404 && responseBody.contains("Conversation Not Exists") && conversationId != null) {
                log.warn("检测到会话 ID 已失效，尝试重新发起请求（不带会话 ID）");
                return chat(message, null, userId, files);
            }
            
            DifyChatResponse errorResponse = new DifyChatResponse();
            errorResponse.setAnswer("抱歉，AI 助手目前认证或参数有误。详细错误: " + responseBody);
            return errorResponse;
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            log.error("Dify API 调用服务端异常 (HTTP {}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            DifyChatResponse errorResponse = new DifyChatResponse();
            errorResponse.setAnswer("抱歉，Dify 服务端出现异常，请稍后再试。");
            return errorResponse;
        } catch (Exception e) {
            log.error("Dify API 调用未知异常: {}", e.getMessage(), e);
            DifyChatResponse errorResponse = new DifyChatResponse();
            errorResponse.setAnswer("抱歉，系统内部错误，请联系管理员。");
            return errorResponse;
        }
    }

    /**
     * 上传文件到 Dify
     */
    public Object uploadFile(MultipartFile file, String user) throws Exception {
        String uploadUrl = difyApiUrl.replace("/chat-messages", "/files/upload");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(difyApiKey);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        
        // 使用自定义 Resource 确保文件名正确传递给 Dify
        body.add("file", new org.springframework.core.io.ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });
        body.add("user", user);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        
        log.info("转发文件上传请求到 Dify API: {}, fileName: {}", uploadUrl, file.getOriginalFilename());
        return restTemplate.postForObject(uploadUrl, requestEntity, Object.class);
    }

    /**
     * 停止 Dify 响应
     */
    public Object stopResponse(String taskId, String user) throws Exception {
        String stopUrl = difyApiUrl.replace("/chat-messages", "/chat-messages/" + taskId + "/stop");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(difyApiKey);

        Map<String, String> body = new HashMap<>();
        body.put("user", user);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);
        
        log.info("转发停止响应请求到 Dify API: {}, taskId: {}", stopUrl, taskId);
        return restTemplate.postForObject(stopUrl, requestEntity, Object.class);
    }

    /**
     * 获取历史消息
     */
    public Object getMessages(String conversationId, String user, String firstId, Integer limit) {
        StringBuilder urlBuilder = new StringBuilder(difyApiUrl.replace("/chat-messages", "/messages"));
        urlBuilder.append("?user=").append(user)
                  .append("&conversation_id=").append(conversationId);
        
        if (firstId != null && !firstId.isEmpty() && !"null".equals(firstId)) {
            urlBuilder.append("&first_id=").append(firstId);
        }
        
        if (limit != null) {
            urlBuilder.append("&limit=").append(limit);
        }
        
        String messagesUrl = urlBuilder.toString();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(difyApiKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        log.info("转发获取历史消息请求到 Dify API: {}", messagesUrl);
        return restTemplate.exchange(messagesUrl, org.springframework.http.HttpMethod.GET, entity, Object.class).getBody();
    }

    /**
     * 获取会话列表
     */
    public Object getConversations(String user, String lastId, Integer limit, String sortBy) {
        StringBuilder urlBuilder = new StringBuilder(difyApiUrl.replace("/chat-messages", "/conversations"));
        urlBuilder.append("?user=").append(user);
        
        if (lastId != null && !lastId.isEmpty() && !"null".equals(lastId)) {
            urlBuilder.append("&last_id=").append(lastId);
        }
        
        if (limit != null) {
            urlBuilder.append("&limit=").append(limit);
        }
        
        if (sortBy != null && !sortBy.isEmpty()) {
            urlBuilder.append("&sort_by=").append(sortBy);
        }
        
        String conversationsUrl = urlBuilder.toString();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(difyApiKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        log.info("转发获取会话列表请求到 Dify API: {}", conversationsUrl);
        return restTemplate.exchange(conversationsUrl, org.springframework.http.HttpMethod.GET, entity, Object.class).getBody();
    }

    /**
     * 删除会话
     */
    public Object deleteConversation(String conversationId, String user) {
        String deleteUrl = difyApiUrl.replace("/chat-messages", "/conversations/" + conversationId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(difyApiKey);

        Map<String, String> body = new HashMap<>();
        body.put("user", user);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);
        
        log.info("转发删除会话请求到 Dify API: {}, conversationId: {}", deleteUrl, conversationId);
        return restTemplate.exchange(deleteUrl, org.springframework.http.HttpMethod.DELETE, requestEntity, Object.class).getBody();
    }
}
