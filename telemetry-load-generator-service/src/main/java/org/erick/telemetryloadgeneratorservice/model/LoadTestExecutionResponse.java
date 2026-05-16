package org.erick.telemetryloadgeneratorservice.model;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Current state and counters for a telemetry load test execution.")
public class LoadTestExecutionResponse {

    @Schema(description = "Unique execution identifier.", example = "2f6f0f45-42f0-4e2d-a4be-6e59413c0a33")
    private UUID id;
    @Schema(description = "Human-readable execution name.", example = "Teste carga controlada")
    private String name;
    @Schema(description = "Execution lifecycle status.", example = "RUNNING")
    private LoadTestStatus status;
    @Schema(description = "Target HTTP URL receiving telemetry events.", example = "http://localhost:8081/api/telemetry")
    private String targetUrl;
    @Schema(description = "Total events configured for this execution.", example = "10000")
    private int totalEvents;
    @Schema(description = "Configured target dispatch rate.", example = "100")
    private int requestsPerSecond;
    @Schema(description = "Number of HTTP requests dispatched so far.", example = "350")
    private int sentEvents;
    @Schema(description = "Number of 2xx HTTP responses received.", example = "345")
    private int successResponses;
    @Schema(description = "Number of failed HTTP responses or send errors.", example = "5")
    private int failedResponses;
    @Schema(description = "Timestamp when the execution was started.", example = "2026-05-16T10:00:00Z")
    private Instant startedAt;
    @Schema(description = "Timestamp when the execution reached a terminal status.", example = "2026-05-16T10:01:40Z")
    private Instant finishedAt;
    @Schema(description = "Scenario percentage distribution used by this execution.")
    private ScenarioDistribution distribution;

    public static LoadTestExecutionResponse from(LoadTestExecution execution) {
        LoadTestExecutionResponse response = new LoadTestExecutionResponse();
        response.id = execution.getId();
        response.name = execution.getName();
        response.status = execution.getStatus();
        response.targetUrl = execution.getTargetUrl();
        response.totalEvents = execution.getTotalEvents();
        response.requestsPerSecond = execution.getRequestsPerSecond();
        response.sentEvents = execution.getSentEvents();
        response.successResponses = execution.getSuccessResponses();
        response.failedResponses = execution.getFailedResponses();
        response.startedAt = execution.getStartedAt();
        response.finishedAt = execution.getFinishedAt();
        response.distribution = execution.getDistribution();
        return response;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LoadTestStatus getStatus() {
        return status;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public int getTotalEvents() {
        return totalEvents;
    }

    public int getRequestsPerSecond() {
        return requestsPerSecond;
    }

    public int getSentEvents() {
        return sentEvents;
    }

    public int getSuccessResponses() {
        return successResponses;
    }

    public int getFailedResponses() {
        return failedResponses;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public ScenarioDistribution getDistribution() {
        return distribution;
    }
}
