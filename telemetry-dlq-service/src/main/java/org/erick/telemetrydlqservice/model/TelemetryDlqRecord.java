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

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "telemetry_dlq_records")
@Schema(description = "Persisted representation of a telemetry message that reached the dead-letter queue.")
public class TelemetryDlqRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Database identifier of the DLQ record.", example = "42")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Schema(description = "Operational status assigned to this DLQ record.", example = "PENDENTE")
    private TelemetryDlqStatus status = TelemetryDlqStatus.PENDENTE;

    @Schema(description = "Timestamp when the DLQ service persisted the failed message.", example = "2026-05-16T10:00:00Z")
    private Instant dlqTimestamp;
    @Schema(description = "Java exception class captured from the failed processing attempt.", example = "java.lang.IllegalArgumentException")
    private String exceptionClass;

    @Column(length = 2000)
    @Schema(description = "Failure message captured from the processing exception.", example = "Timestamp do evento e invalido")
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Stack trace captured from the processing exception.")
    private String stackTrace;

    @Schema(description = "Original telemetry vehicle identifier.", example = "VH-DLQ-000123")
    private String vehicleId;
    @Schema(description = "Original telemetry event timestamp.", example = "2026-05-16T10:00:00Z")
    private Instant originalTimestamp;
    @Schema(description = "Original telemetry latitude.", example = "-23.106")
    private Double latitude;
    @Schema(description = "Original telemetry longitude.", example = "-49.805")
    private Double longitude;
    @Schema(description = "Original telemetry speed.", example = "80.0")
    private Double speed;
    @Schema(description = "Original telemetry temperature.", example = "85.0")
    private Double temperature;
    @Schema(description = "Original telemetry fuel level.", example = "70.0")
    private Double fuelLevel;
    @Schema(description = "Number of reprocess attempts requested from the DLQ API.", example = "1")
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
