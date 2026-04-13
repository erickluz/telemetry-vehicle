package com.example.vehicleingestionservice.controller;

import com.example.vehicleingestionservice.model.TelemetryEvent;
import com.example.vehicleingestionservice.messaging.TelemetryProducer;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/telemetry")
public class TelemetryController {

    private final TelemetryProducer producer;

    public TelemetryController(TelemetryProducer producer) {
        this.producer = producer;
    }

    @PostMapping
    public ResponseEntity<String> publish(@Valid @RequestBody TelemetryEvent event) {
        producer.sendTelemetry(event);
        return ResponseEntity.accepted().body("Telemetry accepted");
    }
}
