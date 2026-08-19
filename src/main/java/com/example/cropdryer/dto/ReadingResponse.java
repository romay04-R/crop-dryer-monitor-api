package com.example.cropdryer.dto;

import com.example.cropdryer.entity.Reading;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public class ReadingResponse {

    private UUID id;

    @JsonProperty("created_at")
    private Instant createdAt;

    private Double temperature;
    private Double humidity;
    private Integer water;

    @JsonProperty("fan_on")
    private Boolean fanOn;

    private Integer rain;

    @JsonProperty("rain_status")
    private String rainStatus;

    private String alarm;

    @JsonProperty("device_id")
    private String deviceId;

    public static ReadingResponse fromEntity(Reading r) {
        ReadingResponse resp = new ReadingResponse();
        resp.setId(r.getId());
        resp.setCreatedAt(r.getCreatedAt());
        resp.setTemperature(r.getTemperature());
        resp.setHumidity(r.getHumidity());
        resp.setWater(r.getWater());
        resp.setFanOn(r.getFanOn());
        resp.setRain(r.getRain());
        resp.setRainStatus(r.getRainStatus());
        resp.setAlarm(r.getAlarm());
        resp.setDeviceId(r.getDeviceId());
        return resp;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public Integer getWater() {
        return water;
    }

    public void setWater(Integer water) {
        this.water = water;
    }

    public Boolean getFanOn() {
        return fanOn;
    }

    public void setFanOn(Boolean fanOn) {
        this.fanOn = fanOn;
    }

    public Integer getRain() {
        return rain;
    }

    public void setRain(Integer rain) {
        this.rain = rain;
    }

    public String getRainStatus() {
        return rainStatus;
    }

    public void setRainStatus(String rainStatus) {
        this.rainStatus = rainStatus;
    }

    public String getAlarm() {
        return alarm;
    }

    public void setAlarm(String alarm) {
        this.alarm = alarm;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
