package com.example.vehicletelemetrydashboard.repository;

import com.example.shared.model.VehicleStatus;
import com.example.vehicletelemetrydashboard.model.VehicleEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<VehicleEntity, String> {

    long countByStatus(VehicleStatus status);

    Optional<VehicleEntity> findByInfoPlateIgnoreCase(String plate);
}
