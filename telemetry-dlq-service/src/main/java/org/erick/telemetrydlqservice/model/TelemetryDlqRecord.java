package org.erick.telemetrydlqservice.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import org.erick.shared.model.TelemetryDlqStatus;

@Entity
@Table(name = "telemetry_dlq_records")
public class TelemetryDlqRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TelemetryDlqStatus status = TelemetryDlqStatus.PENDENTE;

    private Instant dlqTimestamp;
    private String exceptionClass;

    @Column(length = 2000)
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String stackTrace;

    private String vehicleId;
    private Instant originalTimestamp;
    private Double latitude;
    private Double longitude;
    private Double speed;
    private Double temperature;
    private Double fuelLevel;
    private Integer reprocessCount = 0;

    @PrePersist
    void prePersist() {
        if (status == null) {
            status = TelemetryDlqStatus.PENDENTE;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TelemetryDlqStatus getStatus() {
        return status;
    }

    public void setStatus(TelemetryDlqStatus status) {
        this.status = status;
    }

    public Instant getDlqTimestamp() {
        return dlqTimestamp;
    }

    public void setDlqTimestamp(Instant dlqTimestamp) {
        this.dlqTimestamp = dlqTimestamp;
    }

    public String getExceptionClass() {
        return exceptionClass;
    }

    public void setExceptionClass(String exceptionClass) {
        this.exceptionClass = exceptionClass;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Instant getOriginalTimestamp() {
        return originalTimestamp;
    }

    public void setOriginalTimestamp(Instant originalTimestamp) {
        this.originalTimestamp = originalTimestamp;
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

    public Integer getReprocessCount() {
        return reprocessCount;
    }

    public void setReprocessCount(Integer reprocessCount) {
        this.reprocessCount = reprocessCount;
    }
}
