package com.sih.roadassistant.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class WeatherService {
    private final RestTemplate restTemplate= new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean checkHeavyRain(double lat, double lng){
        String url = String.format("https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&current=rain",
                lat, lng);
        try{
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            double rainMm = root.path("current").path("rain").asDouble();
            return rainMm>5.0;
        } catch(Exception e){
            System.err.println("Failed to fetch rain telemenatary:"+e.getMessage());
            return false;
        }
    }
}
