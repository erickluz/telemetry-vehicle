package org.erick.vehicletelemetrydashboard.repository;

import org.erick.vehicletelemetrydashboard.model.TelemetryRecordView;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TelemetryRecordViewRepository extends JpaRepository<TelemetryRecordView, Long> {

    @Query(value = """
            SELECT *
            FROM (
                SELECT DISTINCT ON (vehicle_id) *
                FROM telemetry_record
                ORDER BY vehicle_id, "timestamp" DESC, id DESC
            ) latest_per_vehicle
            ORDER BY "timestamp" DESC, id DESC
            LIMIT 10
            """, nativeQuery = true)
    List<TelemetryRecordView> findTop10ByOrderByTimestampDesc();

    Optional<TelemetryRecordView> findTopByOrderByTimestampDesc();
}
