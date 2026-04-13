package com.example.vehicleingestionservice.controller;

import com.example.shared.model.TelemetryEvent;
import com.example.vehicleingestionservice.messaging.TelemetryProducer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/telemetry")
@Tag(name = "Telemetria", description = "Endpoints para ingestão de dados de telemetria de veículos")
public class TelemetryController {

    private final TelemetryProducer producer;

    public TelemetryController(TelemetryProducer producer) {
        this.producer = producer;
    }

    @PostMapping
    @Operation(summary = "Ingerir dados de telemetria", 
               description = "Recebe dados de telemetria de um veículo e os envia para processamento via RabbitMQ")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "202",
            description = "Telemetria aceita para processamento",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = "{\"message\": \"Telemetry accepted\"}")
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Requisição inválida - dados de telemetria com validação falha",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno do servidor ao processar telemetria",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<String> publish(@Valid @RequestBody TelemetryEvent event) {
        producer.sendTelemetry(event);
        return ResponseEntity.accepted().body("Telemetry accepted");
    }
}
