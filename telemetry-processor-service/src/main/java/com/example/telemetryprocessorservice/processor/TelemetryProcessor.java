package com.example.telemetryprocessorservice.processor;

import com.example.telemetryprocessorservice.model.AlertEvent;
import com.example.telemetryprocessorservice.model.TelemetryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class TelemetryProcessor {

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
        // TODO: aplicar regras de negócio e enviar alertas.
        AlertEvent alert = new AlertEvent();
        alert.setVehicleId(event.getVehicleId());
        alert.setTimestamp(event.getTimestamp());
        alert.setAlertType("SAMPLE_ALERT");
        alert.setDescription("Derived alert from telemetry processor");
        rabbitTemplate.convertAndSend(ALERT_EXCHANGE, ALERT_ROUTING_KEY, alert);
    }
}
