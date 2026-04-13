package org.erick.telemetryprocessorservice.repository;

import org.erick.telemetryprocessorservice.model.TelemetryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TelemetryRecordRepository extends JpaRepository<TelemetryRecord, Long> {
}
