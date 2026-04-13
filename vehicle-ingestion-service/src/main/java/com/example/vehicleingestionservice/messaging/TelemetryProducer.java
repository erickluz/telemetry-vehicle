package com.example.vehicleingestionservice.messaging;

import com.example.vehicleingestionservice.model.TelemetryEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class TelemetryProducer {

    private static final String TELEMETRY_EXCHANGE = "telemetry.events.exchange";
    private static final String TELEMETRY_ROUTING_KEY = "telemetry.events";

    private final RabbitTemplate rabbitTemplate;

    public TelemetryProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendTelemetry(TelemetryEvent event) {
        rabbitTemplate.convertAndSend(TELEMETRY_EXCHANGE, TELEMETRY_ROUTING_KEY, event);
    }
}
