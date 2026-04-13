package org.erick.vehicletelemetrydashboard.service;

import org.erick.shared.model.AlertEvent;
import org.erick.shared.model.TelemetryEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BrokerEventBufferService {

    private static final int MAX_EVENTS = 20;

    private final Deque<TelemetryEvent> telemetryEvents = new ArrayDeque<>();
    private final Deque<AlertEvent> alertEvents = new ArrayDeque<>();

    public synchronized void addTelemetryEvent(TelemetryEvent event) {
        addToBuffer(telemetryEvents, event);
    }

    public synchronized void addAlertEvent(AlertEvent event) {
        addToBuffer(alertEvents, event);
    }

    public synchronized List<TelemetryEvent> getRecentTelemetryEvents() {
        return new ArrayList<>(telemetryEvents);
    }

    public synchronized List<AlertEvent> getRecentAlertEvents() {
        return new ArrayList<>(alertEvents);
    }

    private <T> void addToBuffer(Deque<T> buffer, T event) {
        buffer.addFirst(event);
        while (buffer.size() > MAX_EVENTS) {
            buffer.removeLast();
        }
    }
}
