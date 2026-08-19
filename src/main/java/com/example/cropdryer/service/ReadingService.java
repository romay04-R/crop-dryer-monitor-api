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
        Reading r = new Reading();
        r.setTemperature(req.getTemperature());
        r.setHumidity(req.getHumidity());
        r.setWater(req.getWater());
        r.setFanOn(req.getFanOn());
        r.setRain(req.getRain());
        r.setRainStatus(req.getRainStatus() == null ? null : req.getRainStatus().toUpperCase());
        r.setAlarm(req.getAlarm() == null ? null : req.getAlarm().toUpperCase());
        r.setDeviceId(req.getDeviceId());
        Reading saved = repository.save(r);
        
        // Send SMS alert if alarm is WARN or HIGH, or if it's raining
        boolean isRaining = "RAIN".equals(saved.getRainStatus()) || "HEAVY".equals(saved.getRainStatus());
        if ("WARN".equals(saved.getAlarm()) || "HIGH".equals(saved.getAlarm()) || isRaining) {
            smsService.sendAlert(saved);
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
}
