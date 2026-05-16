package org.erick.telemetryloadgeneratorservice.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.erick.shared.model.TelemetryEvent;
import org.erick.telemetryloadgeneratorservice.model.LoadTestExecution;
import org.erick.telemetryloadgeneratorservice.model.LoadTestStatus;
import org.erick.telemetryloadgeneratorservice.model.StartLoadTestRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PreDestroy;

@Service
public class LoadTestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadTestService.class);

    private final ConcurrentHashMap<UUID, LoadTestExecution> executions = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final TelemetryPayloadFactory payloadFactory;
    private final String defaultTargetUrl;

    public LoadTestService(HttpClient httpClient,
                           ObjectMapper objectMapper,
                           TelemetryPayloadFactory payloadFactory,
                           @Value("${telemetry.ingestion.default-target-url}") String defaultTargetUrl) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.payloadFactory = payloadFactory;
        this.defaultTargetUrl = defaultTargetUrl;
    }

    public LoadTestExecution start(StartLoadTestRequest request) {
        validateRequest(request);
        String targetUrl = resolveTargetUrl(request.getTargetUrl());
        LoadTestExecution execution = new LoadTestExecution(
                UUID.randomUUID(),
                request.getName().trim(),
                targetUrl,
                request.getTotalEvents(),
                request.getRequestsPerSecond(),
                request.getDistribution());

        execution.setStatus(LoadTestStatus.RUNNING);
        executions.put(execution.getId(), execution);
        execution.setTask(executorService.submit(() -> runExecution(execution)));

        LOGGER.info("Load test {} started: name={}, targetUrl={}, totalEvents={}, requestsPerSecond={}, distribution={}",
                execution.getId(), execution.getName(), execution.getTargetUrl(),
                execution.getTotalEvents(), execution.getRequestsPerSecond(), execution.getDistribution());

        return execution;
    }

    public LoadTestExecution get(UUID id) {
        LoadTestExecution execution = executions.get(id);
        if (execution == null) {
            throw new LoadTestNotFoundException(id);
        }
        return execution;
    }

    public Collection<LoadTestExecution> list() {
        return executions.values().stream()
                .sorted(Comparator.comparing(LoadTestExecution::getStartedAt).reversed())
                .toList();
    }

    public LoadTestExecution stop(UUID id) {
        LoadTestExecution execution = get(id);
        if (execution.getStatus() == LoadTestStatus.RUNNING) {
            execution.requestStop();
            LOGGER.info("Manual stop requested for load test {}", id);
        }
        return execution;
    }

    private void runExecution(LoadTestExecution execution) {
        long intervalNanos = 1_000_000_000L / execution.getRequestsPerSecond();
        long nextDispatchNanos = System.nanoTime();
        List<CompletableFuture<Void>> responses = new ArrayList<>(execution.getTotalEvents());

        try {
            for (int sequence = 1; sequence <= execution.getTotalEvents(); sequence++) {
                if (execution.isStopRequested()) {
                    waitForResponses(responses);
                    finish(execution, LoadTestStatus.STOPPED);
                    return;
                }

                waitUntil(nextDispatchNanos);
                responses.add(sendEvent(execution, sequence));
                nextDispatchNanos += intervalNanos;
            }

            waitForResponses(responses);
            finish(execution, LoadTestStatus.COMPLETED);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LoadTestStatus status = execution.isStopRequested() ? LoadTestStatus.STOPPED : LoadTestStatus.FAILED;
            finish(execution, status);
        } catch (Exception e) {
            LOGGER.error("Load test {} failed", execution.getId(), e);
            finish(execution, LoadTestStatus.FAILED);
        }
    }

    private CompletableFuture<Void> sendEvent(LoadTestExecution execution, int sequence) {
        TelemetryEvent event = payloadFactory.create(sequence, execution.getDistribution());
        execution.incrementSentEvents();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(execution.getTargetUrl()))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(event)))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .handle((response, error) -> {
                        if (error != null) {
                            execution.incrementFailedResponses();
                            LOGGER.warn("Error sending telemetry event {} for load test {} to {}: {}",
                                    event.getVehicleId(), execution.getId(), execution.getTargetUrl(), error.getMessage());
                            return null;
                        }

                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
                            execution.incrementSuccessResponses();
                        } else {
                            execution.incrementFailedResponses();
                            LOGGER.warn("Telemetry event {} for load test {} returned HTTP {}",
                                    event.getVehicleId(), execution.getId(), response.statusCode());
                        }
                        return null;
                    });
        } catch (JsonProcessingException | IllegalArgumentException e) {
            execution.incrementFailedResponses();
            LOGGER.warn("Error sending telemetry event {} for load test {} to {}: {}",
                    event.getVehicleId(), execution.getId(), execution.getTargetUrl(), e.getMessage());
            return CompletableFuture.completedFuture(null);
        }
    }

    private void waitForResponses(List<CompletableFuture<Void>> responses) {
        CompletableFuture.allOf(responses.toArray(CompletableFuture[]::new)).join();
    }

    private void waitUntil(long targetNanoTime) throws InterruptedException {
        long sleepNanos = targetNanoTime - System.nanoTime();
        if (sleepNanos <= 0) {
            return;
        }
        Thread.sleep(sleepNanos / 1_000_000L, (int) (sleepNanos % 1_000_000L));
    }

    private void finish(LoadTestExecution execution, LoadTestStatus status) {
        execution.setStatus(status);
        execution.setFinishedAt(Instant.now());
        LOGGER.info("Load test {} finished with status {}. sentEvents={}, successResponses={}, failedResponses={}",
                execution.getId(), status, execution.getSentEvents(),
                execution.getSuccessResponses(), execution.getFailedResponses());
    }

    private void validateRequest(StartLoadTestRequest request) {
        if (request.getDistribution().totalPercentage() != 100) {
            throw new InvalidLoadTestRequestException("A soma dos percentuais da distribution deve ser 100");
        }

        if (request.getTargetUrl() != null && request.getTargetUrl().isBlank()) {
            throw new InvalidLoadTestRequestException("targetUrl nao pode ser vazio");
        }
    }

    private String resolveTargetUrl(String targetUrl) {
        if (targetUrl == null) {
            return defaultTargetUrl;
        }
        return targetUrl.trim();
    }

    @PreDestroy
    void shutdown() {
        executorService.shutdownNow();
    }
}
