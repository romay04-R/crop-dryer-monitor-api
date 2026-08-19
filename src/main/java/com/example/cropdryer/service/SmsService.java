package com.example.cropdryer.service;

import com.example.cropdryer.entity.Reading;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class SmsService {

    @Value("${cropdryer.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${cropdryer.sms.twilio.account-sid:}")
    private String accountSid;

    @Value("${cropdryer.sms.twilio.auth-token:}")
    private String authToken;

    @Value("${cropdryer.sms.twilio.from-number:}")
    private String fromNumber;

    @Value("${cropdryer.sms.twilio.to-numbers:}")
    private String toNumbersString;

    public void sendAlert(Reading reading) {
        if (!smsEnabled) {
            log.debug("SMS is disabled, skipping alert notification");
            return;
        }

        if (accountSid == null || accountSid.isBlank() ||
            authToken == null || authToken.isBlank() ||
            fromNumber == null || fromNumber.isBlank() ||
            toNumbersString == null || toNumbersString.isBlank()) {
            log.warn("SMS configuration incomplete, skipping alert notification");
            return;
        }

        try {
            Twilio.init(accountSid, authToken);

            List<String> toNumbers = Arrays.stream(toNumbersString.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            String messageBody = buildAlertMessage(reading);

            for (String toNumber : toNumbers) {
                Message message = Message.creator(
                        new PhoneNumber(toNumber),
                        new PhoneNumber(fromNumber),
                        messageBody
                ).create();

                log.info("SMS alert sent to {}: SID={}", toNumber, message.getSid());
            }
        } catch (Exception e) {
            log.error("Failed to send SMS alert", e);
        }
    }

    private String buildAlertMessage(Reading reading) {
        StringBuilder sb = new StringBuilder();
        sb.append("🚨 CROP DRYER ALERT\n");
        sb.append("Alarm: ").append(reading.getAlarm()).append("\n");
        sb.append("Temperature: ").append(reading.getTemperature()).append("°C\n");
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
