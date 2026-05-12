package org.erick.vehicletelemetrydashboard.messaging;

import org.erick.shared.messaging.RabbitMQConstants;
import org.erick.shared.model.AlertEvent;
import org.erick.shared.model.TelemetryEvent;
import org.erick.vehicletelemetrydashboard.service.BrokerEventBufferService;
import org.erick.vehicletelemetrydashboard.service.VehicleService;
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

    @RabbitListener(queues = RabbitMQConstants.Queues.DASHBOARD_TELEMETRY)
    public void consumeTelemetry(TelemetryEvent event) {
        brokerEventBufferService.addTelemetryEvent(event);
        vehicleService.updateLatestTelemetry(event.getVehicleId(), event);
    }

    @RabbitListener(queues = RabbitMQConstants.Queues.DASHBOARD_ALERTS)
    public void consumeAlert(AlertEvent event) {
        brokerEventBufferService.addAlertEvent(event);
    }
}
