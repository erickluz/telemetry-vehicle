package com.example.vehicleingestionservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

@Schema(description = "Event de telemetria do veículo contendo dados de localização e performance")
public class TelemetryEvent {

    @Schema(description = "Identificador único do veículo", example = "VEHICLE-001")
    @NotBlank
    private String vehicleId;

    @Schema(description = "Timestamp do evento em formato ISO-8601", example = "2026-04-13T10:30:00Z")
    @NotNull
    private Instant timestamp;

    @Schema(description = "Latitude da localização do veículo", example = "-23.5505")
    @NotNull
    private Double latitude;

    @Schema(description = "Longitude da localização do veículo", example = "-46.6333")
    @NotNull
    private Double longitude;

    @Schema(description = "Velocidade do veículo em km/h", example = "65.5", minimum = "0")
    @Min(0)
    private Double speed;

    @Schema(description = "Temperatura do motor em graus Celsius", example = "85.0", minimum = "0")
    @Min(0)
    private Double temperature;

    @Schema(description = "Nível de combustível em percentual", example = "75.5", minimum = "0")
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
