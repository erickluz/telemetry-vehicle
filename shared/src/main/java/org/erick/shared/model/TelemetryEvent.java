package org.erick.shared.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public class TelemetryEvent {

    @NotBlank
    private String vehicleId;

    @NotNull
    private Instant timestamp;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    @Min(0)
    private Double speed;

    @Min(0)
    private Double temperature;

    @Min(0)
    private Double fuelLevel;

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

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

    public GeoLocation getLocation() {
        GeoLocation location = new GeoLocation();
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }

    public void setLocation(GeoLocation location) {
        if (location == null) {
            this.latitude = null;
            this.longitude = null;
            return;
        }
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
    }

    @Override
    public String toString() {
        return "TelemetryEvent{" +
                "vehicleId='" + vehicleId + '\'' +
                ", timestamp=" + timestamp +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", speed=" + speed +
                ", temperature=" + temperature +
                ", fuelLevel=" + fuelLevel +
                '}';
    }
}
