package com.soccer.forum.service.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

@Component
public class FootballDataApiClient {

    private static final Logger log = LoggerFactory.getLogger(FootballDataApiClient.class);

    private final String apiKey;
    private final String baseUrl;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Rate limiting: 10 requests per minute = 1 request every 6 seconds.
    // To be safe, we use 7 seconds.
    private static final long REQUEST_INTERVAL_MS = 7000;
    private long lastRequestTime = 0;

    public FootballDataApiClient(
            @Value("${football-data.key}") String apiKey,
            @Value("${football-data.base-url}") String baseUrl,
            @Value("${football-data.proxy.host:}") String proxyHost,
            @Value("${football-data.proxy.port:0}") int proxyPort) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.objectMapper = new ObjectMapper();

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        if (proxyHost != null && !proxyHost.isEmpty() && proxyPort > 0) {
            log.info("Configuring proxy: {}:{}", proxyHost, proxyPort);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            factory.setProxy(proxy);
        }
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(10000);
        this.restTemplate = new RestTemplate(factory);
    }

    private synchronized void enforceRateLimit() {
        long now = System.currentTimeMillis();
        long timeSinceLastRequest = now - lastRequestTime;
        if (timeSinceLastRequest < REQUEST_INTERVAL_MS) {
            long sleepTime = REQUEST_INTERVAL_MS - timeSinceLastRequest;
            try {
                log.info("Rate limiting: sleeping for {} ms", sleepTime);
                TimeUnit.MILLISECONDS.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        lastRequestTime = System.currentTimeMillis();
    }

    public JsonNode getTeamSquad(Integer teamId) {
        enforceRateLimit();
        try {
            String url = baseUrl + "/teams/" + teamId;
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Auth-Token", apiKey);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            log.info("Requesting Football-Data.org: {}", url);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readTree(response.getBody());
            }
        } catch (HttpClientErrorException.Forbidden e) {
            log.error("Football-Data.org Forbidden (403): Check API Key or Permissions. {}", e.getMessage());
        } catch (Exception e) {
            log.error("Football-Data.org request failed: {}", e.getMessage());
        }
        return null;
    }

    public JsonNode getPerson(Integer personId) {
        enforceRateLimit();
        try {
            String url = baseUrl + "/persons/" + personId;
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Auth-Token", apiKey);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            log.info("Requesting Football-Data.org: {}", url);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readTree(response.getBody());
            }
        } catch (Exception e) {
            log.error("Football-Data.org request failed: {}", e.getMessage());
        }
        return null;
    }

    public JsonNode getScorers(String competitionCode) {
        enforceRateLimit();
        try {
            String url = baseUrl + "/competitions/" + competitionCode + "/scorers?limit=50";
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Auth-Token", apiKey);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            log.info("Requesting Football-Data.org Scorers: {}", url);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return objectMapper.readTree(response.getBody());
            }
        } catch (Exception e) {
            log.error("Football-Data.org Scorers request failed: {}", e.getMessage());
        }
        return null;
    }
}
