package org.erick.vehicleingestionservice.controller;

import org.erick.shared.model.TelemetryEvent;
import org.erick.vehicleingestionservice.messaging.TelemetryProducer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
@Tag(
        name = "Telemetry Ingestion",
        description = "Receives validated vehicle telemetry events through HTTP and forwards them to the telemetry.events RabbitMQ flow.")
public class TelemetryController {

    private final TelemetryProducer producer;

    public TelemetryController(TelemetryProducer producer) {
        this.producer = producer;
    }

    @PostMapping
    @Operation(
            summary = "Submit a telemetry event",
            description = """
                    Accepts a single telemetry event produced by a vehicle or simulator.
                    The request is validated against the shared TelemetryEvent contract and, when valid,
                    is published asynchronously to RabbitMQ for downstream processing by telemetry-processor-service.
                    """)
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "202",
            description = "Telemetry event accepted and queued for processing",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(example = "{\"message\": \"Telemetry accepted\"}")
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid telemetry payload. Required fields or validation constraints failed.",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal error while publishing telemetry to the broker",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<String> publish(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Telemetry payload compatible with the shared TelemetryEvent model.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TelemetryEvent.class),
                            examples = @ExampleObject(
                                    name = "normalTelemetryEvent",
                                    summary = "Normal vehicle telemetry",
                                    value = """
                                            {
                                              "vehicleId": "VH-NORMAL-000001",
                                              "latitude": -23.106,
                                              "longitude": -49.805,
                                              "speed": 80.0,
                                              "temperature": 85.0,
                                              "fuelLevel": 70.0,
                                              "timestamp": "2026-05-16T10:00:00Z"
                                            }
                                            """)))
            @Valid @RequestBody TelemetryEvent event) {
        producer.sendTelemetry(event);
        return ResponseEntity.accepted().body("Telemetry accepted");
    }
}
