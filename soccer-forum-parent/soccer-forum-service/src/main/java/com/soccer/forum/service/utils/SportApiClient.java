package com.soccer.forum.service.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.soccer.forum.service.config.SportApiConfig;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class SportApiClient {

    private final SportApiConfig sportApiConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public SportApiClient(SportApiConfig sportApiConfig) {
        this.sportApiConfig = sportApiConfig;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 获取单个球员详情
     * <p>
     * 对应 API: GET https://sportapi7.p.rapidapi.com/api/v1/player/{id}
     * </p>
     * @param playerId SportAPI 球员ID
     * @return 球员详情 JSON
     */
    public JsonNode getPlayer(Integer playerId) {
        try {
            String url = "https://" + sportApiConfig.getHost() + "/api/v1/player/" + playerId;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-rapidapi-key", sportApiConfig.getKey());
            headers.set("x-rapidapi-host", sportApiConfig.getHost());

            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readTree(response.getBody());
            }
        } catch (Exception e) {
            if (e.getMessage().contains("429") || e.getMessage().contains("quota")) {
                System.out.println("SportAPI quota exceeded (429). Using mock data for player " + playerId);
                return getMockPlayer(playerId);
            }
            throw new RuntimeException("SportAPI调用失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 获取球队球员列表
     * <p>
     * 对应 API: GET https://sportapi7.p.rapidapi.com/api/v1/team/{teamId}/players
     * </p>
     * @param teamId SportAPI 球队ID
     * @return 球员列表 JSON
     */
    public JsonNode getTeamPlayers(Long teamId) {
        try {
            String url = "https://" + sportApiConfig.getHost() + "/api/v1/team/" + teamId + "/players";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-rapidapi-key", sportApiConfig.getKey());
            headers.set("x-rapidapi-host", sportApiConfig.getHost());

            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readTree(response.getBody());
            }
        } catch (Exception e) {
            if (e.getMessage().contains("429") || e.getMessage().contains("quota")) {
                System.out.println("SportAPI quota exceeded (429). Using mock data for team " + teamId);
                return getMockTeamPlayers(teamId);
            }
            throw new RuntimeException("SportAPI调用失败: " + e.getMessage());
        }
        return null;
    }

    public JsonNode getMockTeamPlayers(Long teamId) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            ArrayNode responseArray = objectMapper.createArrayNode();
            
            // Mock 11 players
            for (int i = 1; i <= 11; i++) {
                ObjectNode item = objectMapper.createObjectNode();
                
                ObjectNode player = objectMapper.createObjectNode();
                player.put("id", teamId * 1000 + i);
                player.put("name", "Mock Player " + i);
                player.put("slug", "mock-player-" + i);
                player.put("shortName", "M. Player " + i);
                
                ArrayNode statsArray = objectMapper.createArrayNode();
                ObjectNode stat = objectMapper.createObjectNode();
                ObjectNode games = objectMapper.createObjectNode();
                
                String position = "Midfielder";
                if (i == 1) position = "Goalkeeper";
                else if (i <= 5) position = "Defender";
                else if (i >= 10) position = "Forward";
                
                games.put("position", position);
                stat.set("games", games);
                statsArray.add(stat);
                
                item.set("player", player);
                item.set("statistics", statsArray);
                
                responseArray.add(item);
            }
            
            root.set("response", responseArray);
            return root;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public JsonNode getMockPlayer(Integer playerId) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            ObjectNode playerNode = objectMapper.createObjectNode();
            
            playerNode.put("id", playerId);
            playerNode.put("name", "Mock Player " + playerId);
            playerNode.put("shortName", "M. Player " + playerId);
            playerNode.put("slug", "mock-player-" + playerId);
            playerNode.put("position", "Forward");
            playerNode.put("height", 180);
            playerNode.put("weight", 75);
            playerNode.put("jerseyNumber", 10);
            playerNode.put("preferredFoot", "Right");
            playerNode.put("proposedMarketValue", "50000000");
            playerNode.put("dateOfBirthTimestamp", 946684800); // 2000-01-01
            
            ObjectNode birth = objectMapper.createObjectNode();
            birth.put("date", "2000-01-01");
            birth.put("place", "Mock City");
            birth.put("country", "Mockland");
            playerNode.set("birth", birth);
            
            playerNode.put("contractUntil", "2026-06-30");

            ObjectNode country = objectMapper.createObjectNode();
            country.put("name", "Unknown");
            playerNode.set("country", country);
            
            ObjectNode team = objectMapper.createObjectNode();
            team.put("name", "Mock Team");
            playerNode.set("team", team);
            
            root.set("data", objectMapper.createObjectNode().set("player", playerNode));
            // Also support root level player for different API versions
            root.set("player", playerNode);
            
            return root;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
