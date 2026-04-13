package com.example.vehicletelemetrydashboard.model;

import com.example.shared.model.VehicleStatus;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "vehicles")
public class VehicleEntity {

    @Id
    private String id;

    @Embedded
    private VehicleInfoEmbeddable info = new VehicleInfoEmbeddable();

    @Embedded
    private TelemetrySnapshotEmbeddable latestTelemetry = new TelemetrySnapshotEmbeddable();

    @Enumerated(EnumType.STRING)
    private VehicleStatus status = VehicleStatus.ACTIVE;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public VehicleInfoEmbeddable getInfo() {
        return info;
    }

    public void setInfo(VehicleInfoEmbeddable info) {
        this.info = info;
    }

    public TelemetrySnapshotEmbeddable getLatestTelemetry() {
        return latestTelemetry;
    }

    public void setLatestTelemetry(TelemetrySnapshotEmbeddable latestTelemetry) {
        this.latestTelemetry = latestTelemetry;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }
}
