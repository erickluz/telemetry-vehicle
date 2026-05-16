package org.erick.telemetryloadgeneratorservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Configuration used to start a controlled telemetry load test.")
public class StartLoadTestRequest {

    @NotBlank
    @Schema(description = "Human-readable name used to identify the test execution.", example = "Teste carga controlada")
    private String name;

    @Schema(
            description = "HTTP endpoint that receives telemetry events. When omitted, the configured default ingestion URL is used.",
            example = "http://localhost:8081/api/telemetry")
    private String targetUrl;

    @Positive
    @Schema(description = "Total number of telemetry events to dispatch during the execution.", example = "10000", minimum = "1")
    private int totalEvents;

    @Positive
    @Schema(description = "Target dispatch rate in HTTP requests per second.", example = "100", minimum = "1")
    private int requestsPerSecond;

    @Valid
    @NotNull
    @Schema(description = "Percentage distribution by telemetry scenario. The sum of all fields must be 100.")
    private ScenarioDistribution distribution;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public int getTotalEvents() {
        return totalEvents;
    }

    public void setTotalEvents(int totalEvents) {
        this.totalEvents = totalEvents;
    }

    public int getRequestsPerSecond() {
        return requestsPerSecond;
    }

    public void setRequestsPerSecond(int requestsPerSecond) {
        this.requestsPerSecond = requestsPerSecond;
    }

    public ScenarioDistribution getDistribution() {
        return distribution;
    }

    public void setDistribution(ScenarioDistribution distribution) {
        this.distribution = distribution;
    }
}
