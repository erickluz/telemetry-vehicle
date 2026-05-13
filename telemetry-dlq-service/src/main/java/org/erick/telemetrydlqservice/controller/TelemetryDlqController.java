package org.erick.telemetrydlqservice.controller;

import java.util.List;
import java.util.NoSuchElementException;

import org.erick.telemetrydlqservice.model.TelemetryDlqRecord;
import org.erick.telemetrydlqservice.service.TelemetryDlqService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/dlq/messages")
@Tag(name = "DLQ", description = "Endpoints para consulta e operacao de mensagens da DLQ de telemetria")
public class TelemetryDlqController {

    private final TelemetryDlqService telemetryDlqService;

    public TelemetryDlqController(TelemetryDlqService telemetryDlqService) {
        this.telemetryDlqService = telemetryDlqService;
    }

    @GetMapping
    @Operation(
            summary = "Listar mensagens da DLQ",
            description = "Lista todas as mensagens consumidas da fila telemetry.events.dlq e armazenadas no banco")
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Mensagens encontradas",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = TelemetryDlqRecord.class)))
    })
    public ResponseEntity<List<TelemetryDlqRecord>> list() {
        return ResponseEntity.ok(telemetryDlqService.findAll());
    }

    @PostMapping("/{id}/reprocess")
    @Operation(
            summary = "Reprocessar mensagem da DLQ",
            description = "Publica a mensagem original novamente na fila telemetry.events para reprocessamento")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Mensagem enviada para reprocessamento"),
        @ApiResponse(responseCode = "404", description = "Mensagem da DLQ nao encontrada", content = @Content)
    })
    public ResponseEntity<Void> reprocess(@PathVariable Long id) {
        telemetryDlqService.reprocess(id);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Excluir mensagem da DLQ",
            description = "Remove do banco a mensagem consumida da DLQ pelo ID informado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Mensagem excluida"),
        @ApiResponse(responseCode = "404", description = "Mensagem da DLQ nao encontrada", content = @Content)
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        telemetryDlqService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNotFound(NoSuchElementException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }
}
