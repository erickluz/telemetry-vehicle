package org.erick.telemetrydlqservice.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.erick.shared.messaging.RabbitMQConstants;
import org.erick.shared.model.TelemetryDlqMessage;
import org.erick.shared.model.TelemetryEvent;
import org.erick.telemetrydlqservice.model.TelemetryDlqRecord;
import org.erick.telemetrydlqservice.repository.TelemetryDlqRecordRepository;
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

    public TelemetryDlqRecord save(TelemetryDlqMessage message) {
        TelemetryDlqRecord record = new TelemetryDlqRecord();
        record.setDlqTimestamp(message.getTimestamp());
        record.setExceptionClass(message.getExceptionClass());
        record.setErrorMessage(message.getErrorMessage());
        record.setStackTrace(message.getStackTrace());

        TelemetryEvent originalMessage = message.getOriginalMessage();
        if (originalMessage != null) {
            record.setVehicleId(originalMessage.getVehicleId());
            record.setOriginalTimestamp(originalMessage.getTimestamp());
            record.setLatitude(originalMessage.getLatitude());
            record.setLongitude(originalMessage.getLongitude());
            record.setSpeed(originalMessage.getSpeed());
            record.setTemperature(originalMessage.getTemperature());
            record.setFuelLevel(originalMessage.getFuelLevel());
        }

        return repository.save(record);
    }

    public List<TelemetryDlqRecord> findAll() {
        return repository.findAll();
    }

    public TelemetryDlqRecord findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Mensagem da DLQ nao encontrada: " + id));
    }

    public TelemetryDlqRecord update(Long id, TelemetryDlqRecord updatedRecord) {
        TelemetryDlqRecord record = findById(id);
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
        return repository.save(record);
    }

    public void reprocess(Long id) {
        TelemetryDlqRecord record = findById(id);
        TelemetryEvent event = toTelemetryEvent(record);

        rabbitTemplate.convertAndSend(
                RabbitMQConstants.Exchanges.TELEMETRY_EVENTS,
                RabbitMQConstants.RoutingKeys.TELEMETRY_EVENTS,
                event);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("Mensagem da DLQ nao encontrada: " + id);
        }
        repository.deleteById(id);
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
}
