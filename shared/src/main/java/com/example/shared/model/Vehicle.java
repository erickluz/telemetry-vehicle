package com.example.shared.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class Vehicle {

    @NotBlank
    private String id;

    @Valid
    @NotNull
    private VehicleInfo info;

    @Valid
    private VehicleTelemetryData latestTelemetry;

    @NotNull
    private VehicleStatus status = VehicleStatus.ACTIVE;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public VehicleInfo getInfo() {
        return info;
    }

    public void setInfo(VehicleInfo info) {
        this.info = info;
    }

    public VehicleTelemetryData getLatestTelemetry() {
        return latestTelemetry;
    }

    public void setLatestTelemetry(VehicleTelemetryData latestTelemetry) {
        this.latestTelemetry = latestTelemetry;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }
}
