package com.example.vehicletelemetrydashboard.service;

import com.example.shared.model.AlertEvent;
import com.example.shared.model.TelemetryEvent;
import com.example.shared.model.VehicleStatus;
import com.example.vehicletelemetrydashboard.model.TelemetryRecordView;
import com.example.vehicletelemetrydashboard.repository.TelemetryRecordViewRepository;
import com.example.vehicletelemetrydashboard.repository.VehicleRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final VehicleRepository vehicleRepository;
    private final TelemetryRecordViewRepository telemetryRecordRepository;
    private final BrokerEventBufferService brokerEventBufferService;

    public DashboardService(
            VehicleRepository vehicleRepository,
            TelemetryRecordViewRepository telemetryRecordRepository,
            BrokerEventBufferService brokerEventBufferService) {
        this.vehicleRepository = vehicleRepository;
        this.telemetryRecordRepository = telemetryRecordRepository;
        this.brokerEventBufferService = brokerEventBufferService;
    }

    @Transactional(readOnly = true)
    public DashboardView loadDashboard() {
        return new DashboardView(
                vehicleRepository.count(),
                vehicleRepository.countByStatus(VehicleStatus.ACTIVE),
                vehicleRepository.countByStatus(VehicleStatus.MAINTENANCE),
                vehicleRepository.countByStatus(VehicleStatus.INACTIVE),
                telemetryRecordRepository.count(),
                telemetryRecordRepository.findTopByOrderByTimestampDesc()
                        .map(TelemetryRecordView::getTimestamp)
                        .orElse(null),
                telemetryRecordRepository.findTop10ByOrderByTimestampDesc(),
                brokerEventBufferService.getRecentTelemetryEvents(),
                brokerEventBufferService.getRecentAlertEvents()
        );
    }

    public record DashboardView(
            long totalVehicles,
            long activeVehicles,
            long maintenanceVehicles,
            long inactiveVehicles,
            long telemetryRecords,
            Instant latestDatabaseEvent,
            List<TelemetryRecordView> recentDatabaseTelemetry,
            List<TelemetryEvent> recentBrokerTelemetry,
            List<AlertEvent> recentBrokerAlerts) {
    }
}
