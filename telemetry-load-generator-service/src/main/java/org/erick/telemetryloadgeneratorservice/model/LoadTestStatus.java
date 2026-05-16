package org.erick.telemetryloadgeneratorservice.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lifecycle status of a load test execution.")
public enum LoadTestStatus {
    CREATED,
    RUNNING,
    COMPLETED,
    STOPPED,
    FAILED
}
