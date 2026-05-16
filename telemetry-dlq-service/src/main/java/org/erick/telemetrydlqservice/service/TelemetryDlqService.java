package org.erick.telemetrydlqservice.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.erick.shared.messaging.RabbitMQConstants;
import org.erick.shared.model.TelemetryDlqMessage;
import org.erick.shared.model.TelemetryDlqStatus;
import org.erick.shared.model.TelemetryEvent;
import org.erick.telemetrydlqservice.model.TelemetryDlqRecord;
import org.erick.telemetrydlqservice.repository.TelemetryDlqRecordRepository;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class TelemetryDlqService {

    private final TelemetryDlqRecordRepository repository;
    private final RabbitTemplate rabbitTemplate;

    public TelemetryDlqService(TelemetryDlqRecordRepository repository, RabbitTemplate rabbitTemplate) {
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public TelemetryDlqRecord save(TelemetryDlqMessage message, Message amqpMessage) {
        TelemetryEvent originalMessage = message.getOriginalMessage();
        Long dlqRecordId = getLongHeader(amqpMessage, RabbitMQConstants.Headers.DLQ_RECORD_ID);
        Integer reprocessCount = getIntegerHeader(amqpMessage, RabbitMQConstants.Headers.REPROCESS_COUNT);
        TelemetryDlqRecord record = findReprocessedRecord(dlqRecordId);
        record.setStatus(dlqRecordId != null
                ? TelemetryDlqStatus.FALHA_NO_REPROCESSAMENTO
                : TelemetryDlqStatus.PENDENTE);
        if (reprocessCount != null) {
            record.setReprocessCount(reprocessCount);
        }
        copyMessageToRecord(message, originalMessage, record);

        return repository.save(record);
    }

    public List<TelemetryDlqRecord> findAll(TelemetryDlqStatus status) {
        List<TelemetryDlqRecord> records;
        if (status == null) {
            records = repository.findAll();
        } else if (TelemetryDlqStatus.PENDENTE.equals(status)) {
            records = new java.util.ArrayList<>(repository.findByStatus(status));
            records.addAll(repository.findByStatusIsNull());
        } else {
            records = repository.findByStatus(status);
        }
        records.forEach(this::applyDefaultStatus);
        return records;
    }

    public List<TelemetryDlqRecord> findAll() {
        return findAll(null);
    }

    public TelemetryDlqRecord findById(Long id) {
        TelemetryDlqRecord record = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Mensagem da DLQ nao encontrada: " + id));
        applyDefaultStatus(record);
        return record;
    }

    public TelemetryDlqRecord update(Long id, TelemetryDlqRecord updatedRecord) {
        TelemetryDlqRecord record = findById(id);
        if (updatedRecord.getStatus() != null) {
            record.setStatus(updatedRecord.getStatus());
        }
        record.setDlqTimestamp(updatedRecord.getDlqTimestamp());
        record.setExceptionClass(updatedRecord.getExceptionClass());
        record.setErrorMessage(updatedRecord.getErrorMessage());
        record.setStackTrace(updatedRecord.getStackTrace());
        record.setVehicleId(updatedRecord.getVehicleId());
        record.setOriginalTimestamp(updatedRecord.getOriginalTimestamp());
        record.setLatitude(updatedRecord.getLatitude());
        record.setLongitude(updatedRecord.getLongitude());
        record.setSpeed(updatedRecord.getSpeed());
        record.setTemperature(updatedRecord.getTemperature());
        record.setFuelLevel(updatedRecord.getFuelLevel());
        if (updatedRecord.getReprocessCount() != null) {
            record.setReprocessCount(updatedRecord.getReprocessCount());
        }
        return repository.save(record);
    }

    public TelemetryDlqRecord updateStatus(Long id, TelemetryDlqStatus status) {
        TelemetryDlqRecord record = findById(id);
        record.setStatus(status);
        return repository.save(record);
    }

    public void reprocess(Long id) {
        TelemetryDlqRecord record = findById(id);
        int reprocessCount = nextReprocessCount(record);
        record.setStatus(TelemetryDlqStatus.REPROCESSANDO);
        record.setReprocessCount(reprocessCount);
        repository.save(record);
        TelemetryEvent event = toTelemetryEvent(record);

        rabbitTemplate.convertAndSend(
                RabbitMQConstants.Exchanges.TELEMETRY_EVENTS,
                RabbitMQConstants.RoutingKeys.TELEMETRY_EVENTS,
                event,
                message -> {
                    message.getMessageProperties().setHeader(RabbitMQConstants.Headers.REPROCESSED, true);
                    message.getMessageProperties().setHeader(RabbitMQConstants.Headers.REPROCESS_SOURCE, "telemetry-dlq-service");
                    message.getMessageProperties().setHeader(RabbitMQConstants.Headers.DLQ_RECORD_ID, record.getId());
                    message.getMessageProperties().setHeader(RabbitMQConstants.Headers.REPROCESS_COUNT, reprocessCount);
                    return message;
                });
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("Mensagem da DLQ nao encontrada: " + id);
        }
        repository.deleteById(id);
    }

    private TelemetryDlqRecord findReprocessedRecord(Long dlqRecordId) {
        if (dlqRecordId == null) {
            return new TelemetryDlqRecord();
        }
        return repository.findById(dlqRecordId)
                .orElseGet(TelemetryDlqRecord::new);
    }

    private void copyMessageToRecord(
            TelemetryDlqMessage message,
            TelemetryEvent originalMessage,
            TelemetryDlqRecord record) {
        record.setDlqTimestamp(message.getTimestamp());
        record.setExceptionClass(message.getExceptionClass());
        record.setErrorMessage(message.getErrorMessage());
        record.setStackTrace(message.getStackTrace());

        if (originalMessage != null) {
            record.setVehicleId(originalMessage.getVehicleId());
            record.setOriginalTimestamp(originalMessage.getTimestamp());
            record.setLatitude(originalMessage.getLatitude());
            record.setLongitude(originalMessage.getLongitude());
            record.setSpeed(originalMessage.getSpeed());
            record.setTemperature(originalMessage.getTemperature());
            record.setFuelLevel(originalMessage.getFuelLevel());
        }
    }

    private TelemetryEvent toTelemetryEvent(TelemetryDlqRecord record) {
        TelemetryEvent event = new TelemetryEvent();
        event.setVehicleId(record.getVehicleId());
        event.setTimestamp(record.getOriginalTimestamp());
        event.setLatitude(record.getLatitude());
        event.setLongitude(record.getLongitude());
        event.setSpeed(record.getSpeed());
        event.setTemperature(record.getTemperature());
        event.setFuelLevel(record.getFuelLevel());
        return event;
    }

    private int nextReprocessCount(TelemetryDlqRecord record) {
        return record.getReprocessCount() == null ? 1 : record.getReprocessCount() + 1;
    }

    private void applyDefaultStatus(TelemetryDlqRecord record) {
        if (record.getStatus() == null) {
            record.setStatus(TelemetryDlqStatus.PENDENTE);
        }
    }

    private Long getLongHeader(Message message, String headerName) {
        Object value = message.getMessageProperties().getHeaders().get(headerName);
        if (value instanceof Long longValue) {
            return longValue;
        }
        if (value instanceof Integer integerValue) {
            return integerValue.longValue();
        }
        if (value instanceof String stringValue && !stringValue.isBlank()) {
            try {
                return Long.parseLong(stringValue);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    private Integer getIntegerHeader(Message message, String headerName) {
        Object value = message.getMessageProperties().getHeaders().get(headerName);
        if (value instanceof Integer integerValue) {
            return integerValue;
        }
        if (value instanceof Long longValue) {
            return longValue.intValue();
        }
        if (value instanceof String stringValue && !stringValue.isBlank()) {
            try {
                return Integer.parseInt(stringValue);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }
}
