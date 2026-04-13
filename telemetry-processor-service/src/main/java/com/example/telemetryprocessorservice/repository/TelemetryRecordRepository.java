package com.example.telemetryprocessorservice.repository;

import com.example.telemetryprocessorservice.model.TelemetryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TelemetryRecordRepository extends JpaRepository<TelemetryRecord, Long> {
}
