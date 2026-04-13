package com.example.vehicletelemetrydashboard.messaging;

import com.example.shared.model.AlertEvent;
import com.example.shared.model.TelemetryEvent;
import com.example.vehicletelemetrydashboard.service.BrokerEventBufferService;
import com.example.vehicletelemetrydashboard.service.VehicleService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class DashboardEventConsumer {

    private final BrokerEventBufferService brokerEventBufferService;
    private final VehicleService vehicleService;

    public DashboardEventConsumer(
            BrokerEventBufferService brokerEventBufferService,
            VehicleService vehicleService) {
        this.brokerEventBufferService = brokerEventBufferService;
        this.vehicleService = vehicleService;
    }

    @RabbitListener(queues = "vehicle.dashboard.telemetry")
    public void consumeTelemetry(TelemetryEvent event) {
        brokerEventBufferService.addTelemetryEvent(event);
        vehicleService.updateLatestTelemetry(event.getVehicleId(), event);
    }

    @RabbitListener(queues = "vehicle.dashboard.alerts")
    public void consumeAlert(AlertEvent event) {
        brokerEventBufferService.addAlertEvent(event);
    }
}
