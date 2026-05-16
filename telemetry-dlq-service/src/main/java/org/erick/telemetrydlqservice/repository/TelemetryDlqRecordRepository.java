package org.erick.telemetrydlqservice.repository;

import java.util.List;

import org.erick.shared.model.TelemetryDlqStatus;
import org.erick.telemetrydlqservice.model.TelemetryDlqRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TelemetryDlqRecordRepository extends JpaRepository<TelemetryDlqRecord, Long> {
    List<TelemetryDlqRecord> findByStatus(TelemetryDlqStatus status);

    List<TelemetryDlqRecord> findByStatusIsNull();
}
