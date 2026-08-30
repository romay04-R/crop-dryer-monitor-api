package com.example.cropdryer.service;

import com.example.cropdryer.dto.ReadingRequest;
import com.example.cropdryer.entity.Reading;
import com.example.cropdryer.repository.ReadingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ReadingService {

    private final ReadingRepository repository;
    private final SmsService smsService;

    public Reading saveFromRequest(ReadingRequest req) {
        System.out.println("=== ARDUINO READING RECEIVED ===");
        System.out.println("Temperature: " + req.getTemperature());
        System.out.println("Humidity: " + req.getHumidity());
        System.out.println("Water: " + req.getWater());
        System.out.println("Fan On: " + req.getFanOn());
        System.out.println("Rain: " + req.getRain());
        System.out.println("Rain Status: " + req.getRainStatus());
        System.out.println("Alarm: " + req.getAlarm());
        System.out.println("Device ID: " + req.getDeviceId());
        System.out.println("================================");
        
        Reading r = new Reading();
        r.setTemperature(req.getTemperature());
        r.setHumidity(req.getHumidity());
        r.setWater(req.getWater());
        r.setFanOn(req.getFanOn());
        r.setRain(req.getRain());
        
        // Calculate rain status from sensor value if not provided
        String calculatedRainStatus = calculateRainStatus(req.getRain());
        r.setRainStatus(req.getRainStatus() != null ? req.getRainStatus().toUpperCase() : calculatedRainStatus);
        
        r.setAlarm(req.getAlarm() == null ? null : req.getAlarm().toUpperCase());
        r.setDeviceId(req.getDeviceId());
        Reading saved = repository.save(r);
        
        // Send SMS alert if alarm is WARN or HIGH, or if it's raining
        boolean isRaining = "RAIN".equals(saved.getRainStatus()) || "HEAVY".equals(saved.getRainStatus());
        boolean shouldSendSms = "WARN".equals(saved.getAlarm()) || "HIGH".equals(saved.getAlarm()) || isRaining;
        
        if (shouldSendSms) {
            System.out.println("SMS ALERT TRIGGERED - Alarm: " + saved.getAlarm() + ", Rain: " + saved.getRainStatus());
            smsService.sendAlert(saved);
        } else {
            System.out.println("SMS NOT TRIGGERED - Alarm: " + saved.getAlarm() + ", Rain: " + saved.getRainStatus());
        }
        
        return saved;
    }

    public List<Reading> getRecent(int limit, String deviceId) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Reading> page = (deviceId == null || deviceId.isBlank())
                ? repository.findAllByOrderByCreatedAtDesc(pageable)
                : repository.findByDeviceIdOrderByCreatedAtDesc(deviceId, pageable);
        return page.getContent();
    }

    public Optional<Reading> getLatest(String deviceId) {
        return (deviceId == null || deviceId.isBlank())
                ? repository.findTopByOrderByCreatedAtDesc()
                : repository.findTopByDeviceIdOrderByCreatedAtDesc(deviceId);
    }

    public List<Reading> getAlerts(String deviceId) {
        List<String> alarms = Arrays.asList("WARN", "HIGH");
        return (deviceId == null || deviceId.isBlank())
                ? repository.findByAlarmInOrderByCreatedAtDesc(alarms)
                : repository.findByAlarmInAndDeviceIdOrderByCreatedAtDesc(alarms, deviceId);
    }

    private String calculateRainStatus(Integer rainValue) {
        if (rainValue == null) {
            return "DRY";
        }
        // Rain sensor typically returns lower values when dry, higher when wet
        // Thresholds may need adjustment based on sensor calibration
        if (rainValue < 500) {
            return "DRY";
        } else if (rainValue < 800) {
            return "RAIN";
        } else {
            return "HEAVY";
        }
    }
}
