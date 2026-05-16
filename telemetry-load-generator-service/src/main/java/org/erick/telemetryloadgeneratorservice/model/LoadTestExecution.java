package org.erick.telemetryloadgeneratorservice.model;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadTestExecution {

    private final UUID id;
    private final String name;
    private volatile LoadTestStatus status;
    private final String targetUrl;
    private final int totalEvents;
    private final int requestsPerSecond;
    private final AtomicInteger sentEvents = new AtomicInteger();
    private final AtomicInteger successResponses = new AtomicInteger();
    private final AtomicInteger failedResponses = new AtomicInteger();
    private final Instant startedAt;
    private volatile Instant finishedAt;
    private final ScenarioDistribution distribution;
    private volatile boolean stopRequested;
    private volatile Future<?> task;

    public LoadTestExecution(UUID id, String name, String targetUrl, int totalEvents,
                             int requestsPerSecond, ScenarioDistribution distribution) {
        this.id = id;
        this.name = name;
        this.status = LoadTestStatus.CREATED;
        this.targetUrl = targetUrl;
        this.totalEvents = totalEvents;
        this.requestsPerSecond = requestsPerSecond;
        this.startedAt = Instant.now();
        this.distribution = distribution;
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

    public void setStatus(LoadTestStatus status) {
        this.status = status;
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
        return sentEvents.get();
    }

    public int incrementSentEvents() {
        return sentEvents.incrementAndGet();
    }

    public int getSuccessResponses() {
        return successResponses.get();
    }

    public void incrementSuccessResponses() {
        successResponses.incrementAndGet();
    }

    public int getFailedResponses() {
        return failedResponses.get();
    }

    public void incrementFailedResponses() {
        failedResponses.incrementAndGet();
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    public ScenarioDistribution getDistribution() {
        return distribution;
    }

    public boolean isStopRequested() {
        return stopRequested;
    }

    public void requestStop() {
        this.stopRequested = true;
    }

    public Future<?> getTask() {
        return task;
    }

    public void setTask(Future<?> task) {
        this.task = task;
    }
}
