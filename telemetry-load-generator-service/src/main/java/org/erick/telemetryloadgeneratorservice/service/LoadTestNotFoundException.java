package org.erick.telemetryloadgeneratorservice.service;

import java.util.UUID;

public class LoadTestNotFoundException extends RuntimeException {

    public LoadTestNotFoundException(UUID id) {
        super("Load test execution not found: " + id);
    }
}
