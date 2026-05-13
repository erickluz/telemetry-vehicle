package org.erick.telemetryprocessorservice.processor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.erick.shared.messaging.RabbitMQConstants;
import org.erick.shared.model.AlertEvent;
import org.erick.shared.model.TelemetryDlqMessage;
import org.erick.shared.model.TelemetryEvent;
import org.erick.telemetryprocessorservice.service.TelemetryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.rabbitmq.client.Channel;

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
    public void receiveTelemetry(TelemetryEvent event, Message message, Channel channel) throws IOException {
        LOGGER.info("Received telemetry event for vehicle {}", event.getVehicleId());
        try {
            processaEvento(event);
            confirmarMensagem(message, channel);
        } catch (Exception e) {
            tratarRetryDLQ(event, message, channel, e);
        }
    }

    private void tratarRetryDLQ(TelemetryEvent event, Message message, Channel channel, Exception e) throws IOException {
        int retryCount = getRetryCount(message);
        LOGGER.info("Retry count for message: {}", retryCount);
        if (retryCount >= 3) {
            enviarDLQ(event, e);
            confirmarMensagem(message, channel);
        } else {
            rejeitarMensagem(message, channel);
        }
        LOGGER.error("Erro ao processar mensagem", e);
    }

    private void enviarDLQ(TelemetryEvent event, Exception e) {
        TelemetryDlqMessage dlqMessage = buildDlqMessage(event, e);
        rabbitTemplate.convertAndSend(
            RabbitMQConstants.Exchanges.TELEMETRY_EVENTS,
            RabbitMQConstants.RoutingKeys.TELEMETRY_EVENTS_DLQ,
            dlqMessage);
        LOGGER.info("Enviando mensagem para DLQ: {}", dlqMessage);
    }

    private TelemetryDlqMessage buildDlqMessage(TelemetryEvent event, Exception e) {
        TelemetryDlqMessage dlqMessage = new TelemetryDlqMessage();
        dlqMessage.setTimestamp(Instant.now());
        dlqMessage.setOriginalMessage(event);

        if (e != null) {
            dlqMessage.setExceptionClass(e.getClass().getName());
            dlqMessage.setErrorMessage(e.getMessage());
            dlqMessage.setStackTrace(getStackTrace(e));
        }

        return dlqMessage;
    }

    private String getStackTrace(Exception e) {
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));
        return stackTrace.toString();
    }

    private void confirmarMensagem(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        LOGGER.info("Confirmando mensagem com deliveryTag: {}", deliveryTag);
        channel.basicAck(deliveryTag, false);
    }

    private void rejeitarMensagem(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        LOGGER.info("Rejeitando mensagem com deliveryTag: {}", deliveryTag);
        channel.basicNack(deliveryTag, false, false);
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
        if (event.getTimestamp().isAfter(Instant.now())) {
            throw new IllegalArgumentException("Timestamp do evento é inválido: " + event.getTimestamp());
        }
        telemetryService.saveTelemetryEvent(event);
        if (event.getSpeed() > 100) {
            sendAlert(event);
            LOGGER.info("Evento de alerta telemetria para veículo {}", event.getVehicleId());
        }
        LOGGER.info("Evento de telemetria processado {}", event.getVehicleId());
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
