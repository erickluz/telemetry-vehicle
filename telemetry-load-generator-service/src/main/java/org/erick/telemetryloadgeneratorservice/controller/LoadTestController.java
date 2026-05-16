package org.erick.telemetryloadgeneratorservice.controller;

import java.util.List;
import java.util.UUID;

import org.erick.telemetryloadgeneratorservice.model.LoadTestExecutionResponse;
import org.erick.telemetryloadgeneratorservice.model.StartLoadTestRequest;
import org.erick.telemetryloadgeneratorservice.service.LoadTestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/load-tests")
@Tag(
        name = "Telemetry Load Tests",
        description = "Starts, monitors and stops controlled HTTP load tests against telemetry-ingestion-service.")
public class LoadTestController {

    private final LoadTestService loadTestService;

    public LoadTestController(LoadTestService loadTestService) {
        this.loadTestService = loadTestService;
    }

    @PostMapping
    @Operation(
            summary = "Start a telemetry load test",
            description = """
                    Creates an in-memory load test execution and starts dispatching telemetry events asynchronously.
                    The HTTP response is returned immediately with status RUNNING while the execution continues in the background.
                    Events are sent to targetUrl using the scenario distribution defined in the request.
                    """)
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "202",
                    description = "Load test accepted and started",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoadTestExecutionResponse.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request. totalEvents and requestsPerSecond must be positive, name must be present and distribution must total 100.",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<LoadTestExecutionResponse> start(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Load test configuration, including the ingestion HTTP endpoint and percentage distribution by scenario.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StartLoadTestRequest.class),
                            examples = @ExampleObject(
                                    name = "controlledLoadTest",
                                    summary = "10k events at 100 requests per second",
                                    value = """
                                            {
                                              "name": "Teste carga controlada",
                                              "targetUrl": "http://localhost:8081/api/telemetry",
                                              "totalEvents": 10000,
                                              "requestsPerSecond": 100,
                                              "distribution": {
                                                "NORMAL": 80,
                                                "ALERT": 10,
                                                "RETRYABLE_ERROR": 5,
                                                "DLQ_ERROR": 5
                                              }
                                            }
                                            """)))
            @Valid @RequestBody StartLoadTestRequest request) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(LoadTestExecutionResponse.from(loadTestService.start(request)));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a load test execution",
            description = "Returns the current status, counters and timestamps for a single in-memory load test execution.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Execution found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoadTestExecutionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Execution not found", content = @Content)
    })
    public LoadTestExecutionResponse get(
            @Parameter(description = "Execution identifier returned by POST /api/load-tests", example = "2f6f0f45-42f0-4e2d-a4be-6e59413c0a33")
            @PathVariable UUID id) {
        return LoadTestExecutionResponse.from(loadTestService.get(id));
    }

    @GetMapping
    @Operation(
            summary = "List load test executions",
            description = "Lists all in-memory load test executions, ordered by start time from newest to oldest.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Executions listed",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = LoadTestExecutionResponse.class))))
    })
    public List<LoadTestExecutionResponse> list() {
        return loadTestService.list().stream()
                .map(LoadTestExecutionResponse::from)
                .toList();
    }

    @PostMapping("/{id}/stop")
    @Operation(
            summary = "Stop a running load test",
            description = "Requests a manual stop for a RUNNING execution. The service stops dispatching new events and marks the execution as STOPPED.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Stop requested or execution already not running",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoadTestExecutionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Execution not found", content = @Content)
    })
    public LoadTestExecutionResponse stop(
            @Parameter(description = "Execution identifier returned by POST /api/load-tests", example = "2f6f0f45-42f0-4e2d-a4be-6e59413c0a33")
            @PathVariable UUID id) {
        return LoadTestExecutionResponse.from(loadTestService.stop(id));
    }
}
