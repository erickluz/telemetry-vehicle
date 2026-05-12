package org.erick.vehicleingestionservice.messaging;

import org.erick.shared.messaging.RabbitMQConstants;
import org.erick.shared.model.TelemetryEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class TelemetryProducer {

    private final RabbitTemplate rabbitTemplate;
    private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TelemetryProducer.class);

    public TelemetryProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendTelemetry(TelemetryEvent event) {
        log.info("Enviando evento de telemetria para RabbitMQ: {}", event);
        rabbitTemplate.convertAndSend(
                RabbitMQConstants.Exchanges.TELEMETRY_EVENTS,
                RabbitMQConstants.RoutingKeys.TELEMETRY_EVENTS,
                event);
    }
}
