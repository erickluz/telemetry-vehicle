package org.erick.vehicletelemetrydashboard.model;

import org.erick.shared.model.TelemetryDlqStatus;

public class TelemetryDlqForm {

    private Long id;
    private TelemetryDlqStatus status;
    private String dlqTimestamp;
    private String exceptionClass;
    private String errorMessage;
    private String stackTrace;
    private String vehicleId;
    private String originalTimestamp;
    private Double latitude;
    private Double longitude;
    private Double speed;
    private Double temperature;
    private Double fuelLevel;
    private Integer reprocessCount;

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

    public String getDlqTimestamp() {
        return dlqTimestamp;
    }

    public void setDlqTimestamp(String dlqTimestamp) {
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

    public String getOriginalTimestamp() {
        return originalTimestamp;
    }

    public void setOriginalTimestamp(String originalTimestamp) {
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
