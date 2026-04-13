package org.erick.vehicletelemetrydashboard.repository;

import org.erick.vehicletelemetrydashboard.model.TelemetryRecordView;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelemetryRecordViewRepository extends JpaRepository<TelemetryRecordView, Long> {

    List<TelemetryRecordView> findTop10ByOrderByTimestampDesc();

    Optional<TelemetryRecordView> findTopByOrderByTimestampDesc();
}
