package com.example.vehicletelemetrydashboard.service;

import com.example.shared.model.GeoLocation;
import com.example.shared.model.TelemetryEvent;
import com.example.shared.model.Vehicle;
import com.example.shared.model.VehicleInfo;
import com.example.shared.model.VehicleTelemetryData;
import com.example.vehicletelemetrydashboard.model.TelemetrySnapshotEmbeddable;
import com.example.vehicletelemetrydashboard.model.VehicleEntity;
import com.example.vehicletelemetrydashboard.model.VehicleInfoEmbeddable;
import com.example.vehicletelemetrydashboard.repository.VehicleRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional(readOnly = true)
    public List<Vehicle> findAll() {
        return vehicleRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    public Optional<Vehicle> findById(String id) {
        return vehicleRepository.findById(id).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    public boolean existsById(String id) {
        return vehicleRepository.existsById(id);
    }

    @Transactional
    public Vehicle save(Vehicle vehicle) {
        validatePlateUniqueness(vehicle);
        VehicleEntity entity = vehicleRepository.findById(vehicle.getId()).orElseGet(VehicleEntity::new);
        mergeDomainIntoEntity(vehicle, entity);
        return toDomain(vehicleRepository.save(entity));
    }

    @Transactional
    public void deleteById(String id) {
        vehicleRepository.deleteById(id);
    }

    @Transactional
    public void updateLatestTelemetry(String vehicleId, TelemetryEvent event) {
        vehicleRepository.findById(vehicleId).ifPresent(vehicle -> {
            TelemetrySnapshotEmbeddable snapshot = vehicle.getLatestTelemetry();
            if (snapshot == null) {
                snapshot = new TelemetrySnapshotEmbeddable();
                vehicle.setLatestTelemetry(snapshot);
            }
            snapshot.setTimestamp(event.getTimestamp());
            snapshot.setLatitude(event.getLatitude());
            snapshot.setLongitude(event.getLongitude());
            snapshot.setSpeed(event.getSpeed());
            snapshot.setTemperature(event.getTemperature());
            snapshot.setFuelLevel(event.getFuelLevel());
            vehicleRepository.save(vehicle);
        });
    }

    public Vehicle buildEmptyVehicle() {
        Vehicle vehicle = new Vehicle();
        vehicle.setInfo(new VehicleInfo());
        return vehicle;
    }

    private void validatePlateUniqueness(Vehicle vehicle) {
        vehicleRepository.findByInfoPlateIgnoreCase(vehicle.getInfo().getPlate())
                .filter(existing -> !existing.getId().equals(vehicle.getId()))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Ja existe um veiculo cadastrado com esta placa.");
                });
    }

    private void mergeDomainIntoEntity(Vehicle vehicle, VehicleEntity entity) {
        entity.setId(vehicle.getId());
        entity.setStatus(vehicle.getStatus());

        VehicleInfoEmbeddable info = entity.getInfo();
        if (info == null) {
            info = new VehicleInfoEmbeddable();
            entity.setInfo(info);
        }
        info.setPlate(vehicle.getInfo().getPlate());
        info.setVin(vehicle.getInfo().getVin());
        info.setManufacturer(vehicle.getInfo().getManufacturer());
        info.setModel(vehicle.getInfo().getModel());
        info.setModelYear(vehicle.getInfo().getModelYear());
        info.setColor(vehicle.getInfo().getColor());
    }

    private Vehicle toDomain(VehicleEntity entity) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(entity.getId());
        vehicle.setStatus(entity.getStatus());

        VehicleInfo info = new VehicleInfo();
        info.setPlate(entity.getInfo().getPlate());
        info.setVin(entity.getInfo().getVin());
        info.setManufacturer(entity.getInfo().getManufacturer());
        info.setModel(entity.getInfo().getModel());
        info.setModelYear(entity.getInfo().getModelYear());
        info.setColor(entity.getInfo().getColor());
        vehicle.setInfo(info);

        TelemetrySnapshotEmbeddable latestTelemetry = entity.getLatestTelemetry();
        if (latestTelemetry != null && latestTelemetry.getTimestamp() != null) {
            VehicleTelemetryData telemetryData = new VehicleTelemetryData();
            telemetryData.setTimestamp(latestTelemetry.getTimestamp());
            telemetryData.setSpeed(latestTelemetry.getSpeed());
            telemetryData.setTemperature(latestTelemetry.getTemperature());
            telemetryData.setFuelLevel(latestTelemetry.getFuelLevel());

            GeoLocation location = new GeoLocation();
            location.setLatitude(latestTelemetry.getLatitude());
            location.setLongitude(latestTelemetry.getLongitude());
            telemetryData.setLocation(location);
            vehicle.setLatestTelemetry(telemetryData);
        }

        return vehicle;
    }
}
