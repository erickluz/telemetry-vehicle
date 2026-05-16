package org.erick.telemetryloadgeneratorservice.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Telemetry generation scenario used to choose vehicleId prefix and payload values.")
public enum LoadScenario {
    NORMAL,
    ALERT,
    RETRYABLE_ERROR,
    DLQ_ERROR
}
