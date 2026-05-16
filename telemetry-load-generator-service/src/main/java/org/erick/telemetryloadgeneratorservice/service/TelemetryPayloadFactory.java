package org.erick.telemetryloadgeneratorservice.service;

import java.time.Instant;

import org.erick.shared.model.TelemetryEvent;
import org.erick.telemetryloadgeneratorservice.model.LoadScenario;
import org.erick.telemetryloadgeneratorservice.model.ScenarioDistribution;
import org.springframework.stereotype.Component;

@Component
public class TelemetryPayloadFactory {

    public TelemetryEvent create(int sequence, ScenarioDistribution distribution) {
        LoadScenario scenario = resolveScenario(sequence, distribution);
        TelemetryEvent event = new TelemetryEvent();
        event.setVehicleId("%s-%06d".formatted(vehiclePrefix(scenario), sequence));
        event.setTimestamp(Instant.now());
        event.setLatitude(-23.106 + ((sequence % 100) * 0.001));
        event.setLongitude(-49.805 - ((sequence % 100) * 0.001));

        switch (scenario) {
            case ALERT -> {
                event.setSpeed(140.0);
                event.setTemperature(110.0);
                event.setFuelLevel(5.0);
            }
            case NORMAL, RETRYABLE_ERROR, DLQ_ERROR -> {
                event.setSpeed(80.0);
                event.setTemperature(85.0);
                event.setFuelLevel(70.0);
            }
        }

        return event;
    }

    private LoadScenario resolveScenario(int sequence, ScenarioDistribution distribution) {
        int bucket = ((sequence - 1) % 100) + 1;
        int cumulative = 0;
        for (LoadScenario scenario : LoadScenario.values()) {
            cumulative += distribution.percentageFor(scenario);
            if (bucket <= cumulative) {
                return scenario;
            }
        }
        return LoadScenario.NORMAL;
    }

    private String vehiclePrefix(LoadScenario scenario) {
        return switch (scenario) {
            case NORMAL -> "VH-NORMAL";
            case ALERT -> "VH-ALERT";
            case RETRYABLE_ERROR -> "VH-RETRY";
            case DLQ_ERROR -> "VH-DLQ";
        };
    }
}
