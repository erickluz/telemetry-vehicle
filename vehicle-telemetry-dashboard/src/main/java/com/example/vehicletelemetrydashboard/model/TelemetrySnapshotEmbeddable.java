package com.example.vehicletelemetrydashboard.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.Instant;

@Embeddable
public class TelemetrySnapshotEmbeddable {

    @Column(name = "latest_telemetry_timestamp")
    private Instant timestamp;

    @Column(name = "latest_telemetry_latitude")
    private Double latitude;

    @Column(name = "latest_telemetry_longitude")
    private Double longitude;

    @Column(name = "latest_telemetry_speed")
    private Double speed;

    @Column(name = "latest_telemetry_temperature")
    private Double temperature;

    @Column(name = "latest_telemetry_fuel_level")
    private Double fuelLevel;

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getFuelLevel() {
        return fuelLevel;
    }

    public void setFuelLevel(Double fuelLevel) {
        this.fuelLevel = fuelLevel;
    }
}
