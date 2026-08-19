package com.example.cropdryer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class ReadingRequest {

    @NotNull
    @Min(value = -50, message = "temperature must be at least -50°C")
    @Max(value = 100, message = "temperature must be at most 100°C")
    private Double temperature;

    @NotNull
    @Min(value = 0, message = "humidity must be at least 0%")
    @Max(value = 100, message = "humidity must be at most 100%")
    private Double humidity;

    @NotNull
    @Min(value = 0, message = "water level must be at least 0")
    @Max(value = 10000, message = "water level must be at most 10000")
    private Integer water;

    @NotNull
    @JsonProperty("fan_on")
    private Boolean fanOn;

    @Min(value = 0, message = "rain value must be at least 0")
    @Max(value = 1023, message = "rain value must be at most 1023")
    private Integer rain;

    @Pattern(regexp = "^(DRY|RAIN|HEAVY)$", message = "rain_status must be one of \"DRY\", \"RAIN\", \"HEAVY\"")
    private String rainStatus;

    @NotNull
    @Pattern(regexp = "^(OK|WARN|HIGH)$", message = "alarm must be one of \"OK\", \"WARN\", \"HIGH\"")
    private String alarm;

    @JsonProperty("device_id")
    private String deviceId;

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
