package org.erick.telemetrydlqservice.consumer;

import org.erick.shared.messaging.RabbitMQConstants;
import org.erick.shared.model.TelemetryDlqMessage;
import org.erick.telemetrydlqservice.model.TelemetryDlqRecord;
import org.erick.telemetrydlqservice.service.TelemetryDlqService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TelemetryDlqConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelemetryDlqConsumer.class);

    private final TelemetryDlqService telemetryDlqService;

    public TelemetryDlqConsumer(TelemetryDlqService telemetryDlqService) {
        this.telemetryDlqService = telemetryDlqService;
    }

    @RabbitListener(queues = RabbitMQConstants.Queues.TELEMETRY_EVENTS_DLQ)
    public void receive(TelemetryDlqMessage message) {
        TelemetryDlqRecord record = telemetryDlqService.save(message);
        LOGGER.info("Mensagem da DLQ armazenada com id {}", record.getId());
    }
}
