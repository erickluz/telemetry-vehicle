package org.erick.vehicletelemetrydashboard.messaging;

import org.erick.shared.messaging.RabbitMQConstants;
import org.erick.shared.model.AlertEvent;
import org.erick.shared.model.TelemetryEvent;
import org.erick.vehicletelemetrydashboard.service.BrokerEventBufferService;
import org.erick.vehicletelemetrydashboard.service.VehicleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class DashboardEventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardEventConsumer.class);

    private final BrokerEventBufferService brokerEventBufferService;
    private final VehicleService vehicleService;

    public DashboardEventConsumer(
            BrokerEventBufferService brokerEventBufferService,
            VehicleService vehicleService) {
        this.brokerEventBufferService = brokerEventBufferService;
        this.vehicleService = vehicleService;
    }

    @RabbitListener(queues = RabbitMQConstants.Queues.DASHBOARD_TELEMETRY)
    public void consumeTelemetry(TelemetryEvent event) {
        if (event == null || event.getVehicleId() == null || event.getVehicleId().isBlank()) {
            LOGGER.warn("Ignorando evento de telemetria sem vehicleId: {}", event);
            return;
        }
        brokerEventBufferService.addTelemetryEvent(event);
        vehicleService.updateLatestTelemetry(event.getVehicleId(), event);
    }

    @RabbitListener(queues = RabbitMQConstants.Queues.DASHBOARD_ALERTS)
    public void consumeAlert(AlertEvent event) {
        brokerEventBufferService.addAlertEvent(event);
    }
}
