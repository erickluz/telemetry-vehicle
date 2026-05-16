package org.erick.telemetryprocessorservice.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.erick.shared.model.TelemetryDlqStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TelemetryDlqStatusClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(TelemetryDlqStatusClient.class);

    private final HttpClient httpClient;
    private final String baseUrl;

    public TelemetryDlqStatusClient(
            @Value("${telemetry.dlq-service.base-url:http://localhost:8085}") String baseUrl) {
        this.httpClient = HttpClient.newHttpClient();
        this.baseUrl = baseUrl;
    }

    public void markReprocessed(Long dlqRecordId) {
        if (dlqRecordId == null) {
            return;
        }
        updateStatus(dlqRecordId, TelemetryDlqStatus.REPROCESSADO);
    }

    private void updateStatus(Long dlqRecordId, TelemetryDlqStatus status) {
        String encodedStatus = URLEncoder.encode(status.name(), StandardCharsets.UTF_8);
        URI uri = URI.create(baseUrl + "/api/dlq/messages/" + dlqRecordId + "/status?status=" + encodedStatus);
        HttpRequest request = HttpRequest.newBuilder(uri)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() >= 400) {
                LOGGER.warn("Nao foi possivel atualizar status DLQ {} para {}. HTTP {}",
                        dlqRecordId, status, response.statusCode());
            }
        } catch (IOException ex) {
            LOGGER.warn("Erro de IO ao atualizar status DLQ {} para {}", dlqRecordId, status, ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOGGER.warn("Atualizacao de status DLQ {} interrompida", dlqRecordId, ex);
        }
    }
}
