package org.erick.vehicletelemetrydashboard.service;

import java.util.List;

import org.erick.shared.model.TelemetryDlqStatus;
import org.erick.vehicletelemetrydashboard.model.TelemetryDlqRecordView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class TelemetryDlqClient {

    private final RestClient restClient;

    public TelemetryDlqClient(
            RestClient.Builder restClientBuilder,
            @Value("${telemetry.dlq-service.base-url}") String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    public List<TelemetryDlqRecordView> findAll(TelemetryDlqStatus status) {
        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/api/dlq/messages");
                    if (status != null) {
                        uriBuilder.queryParam("status", status);
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .body(new ParameterizedTypeReference<List<TelemetryDlqRecordView>>() {
                });
    }

    public TelemetryDlqRecordView findById(Long id) {
        return restClient.get()
                .uri("/api/dlq/messages/{id}", id)
                .retrieve()
                .body(TelemetryDlqRecordView.class);
    }

    public TelemetryDlqRecordView update(Long id, TelemetryDlqRecordView message) {
        return restClient.put()
                .uri("/api/dlq/messages/{id}", id)
                .body(message)
                .retrieve()
                .body(TelemetryDlqRecordView.class);
    }

    public void reprocess(Long id) {
        restClient.post()
                .uri("/api/dlq/messages/{id}/reprocess", id)
                .retrieve()
                .toBodilessEntity();
    }

    public TelemetryDlqRecordView updateStatus(Long id, TelemetryDlqStatus status) {
        return restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/dlq/messages/{id}/status")
                        .queryParam("status", status)
                        .build(id))
                .retrieve()
                .body(TelemetryDlqRecordView.class);
    }

    public void delete(Long id) {
        restClient.delete()
                .uri("/api/dlq/messages/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }
}
