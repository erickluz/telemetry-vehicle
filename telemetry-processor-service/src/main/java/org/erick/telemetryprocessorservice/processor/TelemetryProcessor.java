package org.erick.telemetryprocessorservice.processor;

import org.erick.shared.model.AlertEvent;
import org.erick.shared.model.TelemetryEvent;
import org.erick.telemetryprocessorservice.service.TelemetryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TelemetryProcessor {

    @Autowired
    private TelemetryService telemetryService;

    private static final Logger LOGGER = LoggerFactory.getLogger(TelemetryProcessor.class);
    private static final String ALERT_EXCHANGE = "telemetry.alerts.exchange";
    private static final String ALERT_ROUTING_KEY = "telemetry.alerts";

    private final RabbitTemplate rabbitTemplate;

    public TelemetryProcessor(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "telemetry.events")
    public void receiveTelemetry(TelemetryEvent event) {
        LOGGER.info("Received telemetry event for vehicle {}", event.getVehicleId());
        
        telemetryService.saveTelemetryEvent(event);

        if (event.getSpeed() > 100) {
            sendAlert(event);
        }

    }

    private void sendAlert(TelemetryEvent event) {
        AlertEvent alert = new AlertEvent();
        alert.setVehicleId(event.getVehicleId());
        alert.setTimestamp(event.getTimestamp());
        alert.setAlertType("SPEEDING");

        rabbitTemplate.convertAndSend(ALERT_EXCHANGE, ALERT_ROUTING_KEY, alert);
        LOGGER.info("Sent alert for vehicle {}", event.toString());
    }
}
