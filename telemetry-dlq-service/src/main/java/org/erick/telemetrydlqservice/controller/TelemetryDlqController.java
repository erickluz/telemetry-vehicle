package org.erick.telemetrydlqservice.controller;

import java.util.List;
import java.util.NoSuchElementException;

import org.erick.shared.model.TelemetryDlqStatus;
import org.erick.telemetrydlqservice.model.TelemetryDlqRecord;
import org.erick.telemetrydlqservice.service.TelemetryDlqService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/dlq/messages")
@Tag(
        name = "Telemetry DLQ Operations",
        description = "Operational endpoints for querying, updating, reprocessing and deleting telemetry DLQ records.")
public class TelemetryDlqController {

    private final TelemetryDlqService telemetryDlqService;

    public TelemetryDlqController(TelemetryDlqService telemetryDlqService) {
        this.telemetryDlqService = telemetryDlqService;
    }

    @GetMapping
    @Operation(
            summary = "List telemetry DLQ records",
            description = """
                    Lists messages consumed from telemetry.events.dlq and persisted in the DLQ database.
                    Use the optional status filter to focus on pending, reprocessed or discarded operational states.
                    """)
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "DLQ records listed",
                content = @Content(
                        mediaType = "application/json",
                        array = @ArraySchema(schema = @Schema(implementation = TelemetryDlqRecord.class))))
    })
    public ResponseEntity<List<TelemetryDlqRecord>> list(
            @Parameter(
                    description = "Optional status filter for DLQ records.",
                    example = "PENDENTE")
            @RequestParam(value = "status", required = false) TelemetryDlqStatus status) {
        return ResponseEntity.ok(telemetryDlqService.findAll(status));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a telemetry DLQ record",
            description = "Returns the persisted DLQ record, including failure metadata and the original telemetry payload fields.")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "DLQ record found",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = TelemetryDlqRecord.class))),
        @ApiResponse(responseCode = "404", description = "DLQ record not found", content = @Content)
    })
    public ResponseEntity<TelemetryDlqRecord> findById(
            @Parameter(description = "DLQ record database identifier", example = "42")
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(telemetryDlqService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a telemetry DLQ record",
            description = """
                    Updates a stored DLQ record before an operator retries or archives it.
                    This endpoint is useful for correcting editable metadata or payload fields captured from a failed message.
                    """)
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "DLQ record updated",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = TelemetryDlqRecord.class))),
        @ApiResponse(responseCode = "404", description = "DLQ record not found", content = @Content)
    })
    public ResponseEntity<TelemetryDlqRecord> update(
            @Parameter(description = "DLQ record database identifier", example = "42")
            @PathVariable("id") Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Replacement values for the persisted DLQ record.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TelemetryDlqRecord.class),
                            examples = @ExampleObject(
                                    name = "dlqRecordUpdate",
                                    summary = "Correct a failed telemetry record",
                                    value = """
                                            {
                                              "status": "PENDENTE",
                                              "vehicleId": "VH-DLQ-000123",
                                              "speed": 80.0,
                                              "temperature": 85.0,
                                              "fuelLevel": 70.0,
                                              "reprocessCount": 0
                                            }
                                            """)))
            @RequestBody TelemetryDlqRecord updatedRecord) {
        return ResponseEntity.ok(telemetryDlqService.update(id, updatedRecord));
    }

    @PostMapping("/{id}/reprocess")
    @Operation(
            summary = "Reprocess a telemetry DLQ record",
            description = "Publishes the original telemetry payload back to telemetry.events so telemetry-processor-service can process it again.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "DLQ record accepted for reprocessing"),
        @ApiResponse(responseCode = "404", description = "DLQ record not found", content = @Content)
    })
    public ResponseEntity<Void> reprocess(
            @Parameter(description = "DLQ record database identifier", example = "42")
            @PathVariable("id") Long id) {
        telemetryDlqService.reprocess(id);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{id}/status")
    @Operation(
            summary = "Update telemetry DLQ record status",
            description = "Changes the operational status of a persisted DLQ record without modifying the original telemetry payload.")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "DLQ status updated",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = TelemetryDlqRecord.class))),
        @ApiResponse(responseCode = "404", description = "DLQ record not found", content = @Content)
    })
    public ResponseEntity<TelemetryDlqRecord> updateStatus(
            @Parameter(description = "DLQ record database identifier", example = "42")
            @PathVariable("id") Long id,
            @Parameter(description = "New operational status for the DLQ record", example = "REPROCESSADO")
            @RequestParam("status") TelemetryDlqStatus status) {
        return ResponseEntity.ok(telemetryDlqService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a telemetry DLQ record",
            description = "Deletes a persisted DLQ record after the operator decides it no longer needs to be retained.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "DLQ record deleted"),
        @ApiResponse(responseCode = "404", description = "DLQ record not found", content = @Content)
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "DLQ record database identifier", example = "42")
            @PathVariable("id") Long id) {
        telemetryDlqService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNotFound(NoSuchElementException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }
}
