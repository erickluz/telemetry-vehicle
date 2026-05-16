package org.erick.telemetrydlqservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Telemetry DLQ Service API",
                version = "1.0.0",
                description = "Operational API for inspecting failed telemetry messages, editing stored DLQ records, reprocessing events and managing DLQ status.",
                contact = @Contact(name = "Telemetry Team"),
                license = @License(name = "Apache 2.0")))
public class TelemetryDlqServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelemetryDlqServiceApplication.class, args);
    }
}
