package org.erick.vehicletelemetrydashboard.repository;

import org.erick.shared.model.VehicleStatus;
import org.erick.vehicletelemetrydashboard.model.VehicleEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<VehicleEntity, String> {

    long countByStatus(VehicleStatus status);

    Optional<VehicleEntity> findByInfoPlateIgnoreCase(String plate);
}
