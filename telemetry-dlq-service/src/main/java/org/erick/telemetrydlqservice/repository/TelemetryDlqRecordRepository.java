package org.erick.telemetrydlqservice.repository;

import org.erick.telemetrydlqservice.model.TelemetryDlqRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TelemetryDlqRecordRepository extends JpaRepository<TelemetryDlqRecord, Long> {
}
