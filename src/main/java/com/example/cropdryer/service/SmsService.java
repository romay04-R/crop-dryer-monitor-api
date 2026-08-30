package com.example.cropdryer.service;

import com.example.cropdryer.entity.Reading;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SmsService {

    private static final String ARKESEL_API_URL = "https://sms.arkesel.com/api/v2/sms/send";

    @Value("${cropdryer.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${cropdryer.sms.arkesel.api-key:}")
    private String apiKey;

    @Value("${cropdryer.sms.arkesel.sender-id:CropDryer}")
    private String senderId;

    @Value("${cropdryer.sms.arkesel.to-numbers:}")
    private String toNumbersString;

    private final RestTemplate restTemplate;

    public SmsService() {
        this.restTemplate = new RestTemplate();
    }

    public void sendAlert(Reading reading) {
        if (!smsEnabled) {
            log.debug("SMS is disabled, skipping alert notification");
            return;
        }

        if (apiKey == null || apiKey.isBlank() ||
            toNumbersString == null || toNumbersString.isBlank()) {
            log.warn("SMS configuration incomplete, skipping alert notification");
            return;
        }

        try {
            List<String> toNumbers = Arrays.stream(toNumbersString.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            String messageBody = buildAlertMessage(reading);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("sender", senderId);
            requestBody.put("message", messageBody);
            requestBody.put("recipients", toNumbers);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    ARKESEL_API_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("SMS alert sent successfully to {} recipients. Response: {}", 
                        toNumbers.size(), response.getBody());
            } else {
                log.warn("SMS alert request failed with status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Failed to send SMS alert", e);
        }
    }

    private String buildAlertMessage(Reading reading) {
        StringBuilder sb = new StringBuilder();
        sb.append("CROP DRYER ALERT\n");
        sb.append("Alarm: ").append(reading.getAlarm()).append("\n");
        sb.append("Temperature: ").append(reading.getTemperature()).append("C\n");
        sb.append("Humidity: ").append(reading.getHumidity()).append("%\n");
        sb.append("Water Level: ").append(reading.getWater()).append("\n");
        sb.append("Fan: ").append(reading.getFanOn() ? "ON" : "OFF").append("\n");
        boolean isRaining = "RAIN".equals(reading.getRainStatus()) || "HEAVY".equals(reading.getRainStatus());
        sb.append("Raining: ").append(isRaining ? "YES" : "NO").append("\n");
        
        if (reading.getDeviceId() != null && !reading.getDeviceId().isBlank()) {
            sb.append("Device: ").append(reading.getDeviceId()).append("\n");
        }
        
        sb.append("Time: ").append(reading.getCreatedAt());
        
        return sb.toString();
    }
}
