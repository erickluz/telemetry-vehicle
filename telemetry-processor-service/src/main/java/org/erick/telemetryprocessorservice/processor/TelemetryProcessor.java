package org.erick.telemetryprocessorservice.processor;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.erick.shared.messaging.RabbitMQConstants;
import org.erick.shared.model.AlertEvent;
import org.erick.shared.model.TelemetryEvent;
import org.erick.telemetryprocessorservice.service.TelemetryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.AMQP.Channel;

@Component
public class TelemetryProcessor {

    @Autowired
    private TelemetryService telemetryService;

    private static final Logger LOGGER = LoggerFactory.getLogger(TelemetryProcessor.class);
    private final RabbitTemplate rabbitTemplate;

    public TelemetryProcessor(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitMQConstants.Queues.TELEMETRY_EVENTS)
    public void receiveTelemetry(TelemetryEvent event, Message message, Channel channel) {
        LOGGER.info("Received telemetry event for vehicle {}", event.getVehicleId());
        int retryCount = getRetryCount(message);
        LOGGER.debug("Retry count for message: {}", retryCount);

        try {
            processaEvento(event);
        } catch (Exception e) {
        if (retryCount >= 3) {
            // envia para DLQ final
        } else {
            rabbitTemplate.convertAndSend(
                    RabbitMQConstants.Exchanges.TELEMETRY_EVENTS,
                    RabbitMQConstants.RoutingKeys.TELEMETRY_EVENTS_RETRY,
                    event);
        }
        }

    }

    @SuppressWarnings("unchecked")
    private int getRetryCount(Message message) {
        List<Map<String, Object>> xDeath =
                (List<Map<String, Object>>) message.getMessageProperties()
                        .getHeaders()
                        .get("x-death");

        if (xDeath == null || xDeath.isEmpty()) {
            return 0;
        }

        return xDeath.stream()
                .filter(death -> RabbitMQConstants.Queues.TELEMETRY_EVENTS_RETRY.equals(death.get("queue")))
                .map(death -> death.get("count"))
                .filter(Objects::nonNull)
                .mapToInt(count -> ((Long) count).intValue())
                .sum();
    }

    private void processaEvento(TelemetryEvent event) throws Exception {
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

        rabbitTemplate.convertAndSend(
                RabbitMQConstants.Exchanges.TELEMETRY_ALERTS,
                RabbitMQConstants.RoutingKeys.TELEMETRY_EVENTS_ALERTS,
                alert);
        LOGGER.info("Sent alert for vehicle {}", event.toString());
    }
}
