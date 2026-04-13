package com.example.notificationservice.consumer;

import com.example.notificationservice.model.AlertEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AlertConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlertConsumer.class);

    @RabbitListener(queues = "telemetry.alerts")
    public void receiveAlert(AlertEvent event) {
        LOGGER.info("Received alert for vehicle {}: {}", event.getVehicleId(), event.getAlertType());
        // TODO: implementar ação de notificação / simulação de envio
    }
}
