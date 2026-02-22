package com.soccer.forum.service.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.soccer.forum.service.config.RapidApiConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@Component
public class RapidApiClient {

    private final RapidApiConfig rapidApiConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public RapidApiClient(RapidApiConfig rapidApiConfig) {
        this.rapidApiConfig = rapidApiConfig;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public JsonNode get(String endpoint) {
        try {
            String url = "https://" + rapidApiConfig.getHost() + endpoint;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-RapidAPI-Key", rapidApiConfig.getKey());
            headers.set("X-RapidAPI-Host", rapidApiConfig.getHost());

            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readTree(response.getBody());
            }
        } catch (Exception e) {
            if (e.getMessage().contains("429") || e.getMessage().contains("quota")) {
                System.out.println("RapidAPI quota exceeded (429). Using mock data for " + endpoint);
                return getMockData(endpoint);
            }
            throw new RuntimeException("API调用失败: " + e.getMessage());
        }
        return null;
    }

    private JsonNode getMockData(String endpoint) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            
            if (endpoint.contains("/players")) {
                ArrayNode responseArray = objectMapper.createArrayNode();
                // Mock a few players
                for (int i = 1; i <= 5; i++) {
                    ObjectNode item = objectMapper.createObjectNode();
                    ObjectNode player = objectMapper.createObjectNode();
                    player.put("id", 1000 + i);
                    player.put("name", "Mock Rapid Player " + i);
                    player.put("firstname", "Mock");
                    player.put("lastname", "Player " + i);
                    player.put("photo", "https://via.placeholder.com/150");
                    
                    item.set("player", player);
                    
                    ArrayNode stats = objectMapper.createArrayNode();
                    ObjectNode stat = objectMapper.createObjectNode();
                    ObjectNode games = objectMapper.createObjectNode();
                    games.put("position", "Midfielder");
                    stat.set("games", games);
                    stats.add(stat);
                    
                    item.set("statistics", stats);
                    responseArray.add(item);
                }
                root.set("response", responseArray);
            } else {
                root.put("message", "Mock data not implemented for this endpoint");
            }
            
            return root;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
