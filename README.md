# Telemetry Vehicle

## Observability endpoints

Each Spring Boot service exposes the basic Actuator endpoints below for local/default execution. Prometheus and Grafana are not required for the services to start; the Prometheus endpoint is available for future scraping.

| Service | Port | Health | Metrics | Prometheus |
| --- | ---: | --- | --- | --- |
| vehicle-ingestion-service | 8081 | http://localhost:8081/actuator/health | http://localhost:8081/actuator/metrics | http://localhost:8081/actuator/prometheus |
| telemetry-processor-service | 8082 | http://localhost:8082/actuator/health | http://localhost:8082/actuator/metrics | http://localhost:8082/actuator/prometheus |
| notification-service | 8083 | http://localhost:8083/actuator/health | http://localhost:8083/actuator/metrics | http://localhost:8083/actuator/prometheus |
| vehicle-telemetry-dashboard | 8084 | http://localhost:8084/actuator/health | http://localhost:8084/actuator/metrics | http://localhost:8084/actuator/prometheus |
| telemetry-dlq-service | 8085 | http://localhost:8085/actuator/health | http://localhost:8085/actuator/metrics | http://localhost:8085/actuator/prometheus |
| telemetry-load-generator-service | 8086 | http://localhost:8086/actuator/health | http://localhost:8086/actuator/metrics | http://localhost:8086/actuator/prometheus |

Only `health`, `info`, `metrics`, and `prometheus` are exposed through Actuator. Metrics are the default Spring Boot, JVM, HTTP server, Tomcat, and system metrics, with the Micrometer `application` tag set per service.
