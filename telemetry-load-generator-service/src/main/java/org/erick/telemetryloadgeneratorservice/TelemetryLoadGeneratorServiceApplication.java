package org.erick.telemetryloadgeneratorservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Telemetry Load Generator Service API",
                version = "1.0.0",
                description = "API for starting, tracking and stopping controlled telemetry load tests against the ingestion service HTTP endpoint.",
                contact = @Contact(name = "Telemetry Team"),
                license = @License(name = "Apache 2.0")))
public class TelemetryLoadGeneratorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelemetryLoadGeneratorServiceApplication.class, args);
    }
}
