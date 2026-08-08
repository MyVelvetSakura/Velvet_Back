package com.velvet.sakura.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class GeoLocationServiceImpl implements GeoLocationService {

    private final HttpClient httpClient;

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String resolveLocation(String ip) {
        if (ip == null || ip.isBlank()
                || ip.equals("0:0:0:0:0:0:0:1")
                || ip.equals("127.0.0.1")
                || ip.startsWith("192.168.")
                || ip.startsWith("10.")) {
            return "Ubicación no disponible (red local)";
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://ip-api.com/json/" + ip + "?fields=status,country,regionName,city"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode json = mapper.readTree(response.body());

            if ("success".equals(json.path("status").asText())) {
                String city = json.path("city").asText("");
                String region = json.path("regionName").asText("");
                String country = json.path("country").asText("");
                String result = String.join(", ",
                        java.util.stream.Stream.of(city, region, country)
                                .filter(s -> !s.isBlank())
                                .toList());
                return result.isBlank() ? "Ubicación no disponible" : result;
            }
            return "Ubicación no disponible";
        } catch (Exception e) {
            return "Ubicación no disponible";
        }
    }
}