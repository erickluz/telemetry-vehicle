package org.erick.telemetryprocessorservice.service;

import org.erick.shared.model.TelemetryEvent;
import org.erick.telemetryprocessorservice.model.TelemetryRecord;
import org.erick.telemetryprocessorservice.repository.TelemetryRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TelemetryService {

    @Autowired
    private TelemetryRecordRepository telemetryRecordRepository;
    
    public void saveTelemetryEvent(TelemetryEvent event) {
        TelemetryRecord telemetry = new TelemetryRecord();
        telemetry.setVehicleId(event.getVehicleId());
        telemetry.setTimestamp(event.getTimestamp());
        telemetry.setSpeed(event.getSpeed());
        telemetry.setTemperature(event.getTemperature());
        telemetry.setFuelLevel(event.getFuelLevel());

        telemetryRecordRepository.save(telemetry);
    }
}
