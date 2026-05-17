# Observability

Local Prometheus and Grafana stack for the telemetry vehicle services.

## Start Prometheus and Grafana

From the repository root:

```bash
cd observability
docker compose up -d
```

URLs:

- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000
- Grafana login: `admin` / `admin`

## Microservice metrics endpoints

The repository service names differ slightly from some architecture aliases:

- `vehicle-ingestion-service` is the ingestion service.
- `vehicle-telemetry-dashboard` is the dashboard service.
- `telemetry-dlq-service` runs on port `8085`.
- `notification-service` is also scraped because it is a Spring Boot microservice in this repository.

Prometheus scrapes these endpoints through `host.docker.internal`:

| Service | Prometheus endpoint |
| --- | --- |
| vehicle-ingestion-service | http://localhost:8081/actuator/prometheus |
| telemetry-processor-service | http://localhost:8082/actuator/prometheus |
| notification-service | http://localhost:8083/actuator/prometheus |
| vehicle-telemetry-dashboard | http://localhost:8084/actuator/prometheus |
| telemetry-dlq-service | http://localhost:8085/actuator/prometheus |
| telemetry-load-generator-service | http://localhost:8086/actuator/prometheus |
| RabbitMQ Prometheus plugin | http://localhost:15692/metrics |

## Validate Prometheus targets

Open Prometheus and go to:

```text
Status > Targets
```

All running microservices should appear as `UP`. Services that are not currently running will appear as `DOWN`.

## Validate dashboards

1. Start RabbitMQ and PostgreSQL from the project root:

   ```bash
   docker compose up -d
   ```

2. Start the Spring Boot microservices locally.
3. Start the observability stack:

   ```bash
   cd observability
   docker compose up -d
   ```

4. Run traffic through `telemetry-load-generator-service`.
5. Open Grafana at http://localhost:3000 and browse the `Telemetry Vehicle` folder.

Provisioned dashboards:

- Telemetry System Flow Overview
- Telemetry Overview
- HTTP Overview
- JVM Overview
- Load Generator Overview
- RabbitMQ Overview

The main dashboard is **Telemetry System Flow Overview**. It is organized around the operational telemetry path:

```text
telemetry-load-generator-service
  -> vehicle-ingestion-service
  -> RabbitMQ telemetry.events
  -> telemetry-processor-service
  -> Postgres
  -> telemetry.alerts / telemetry.events.retry / telemetry.events.dlq
  -> telemetry-dlq-service
  -> vehicle-telemetry-dashboard
```

## How to open the main dashboard

1. Start the infrastructure and services.
2. Open Grafana at http://localhost:3000.
3. Log in with `admin` / `admin`.
4. Open the `Telemetry Vehicle` folder.
5. Select `Telemetry System Flow Overview`.

## How to start a load test

Start a load test through `telemetry-load-generator-service` using its API or Swagger UI, then watch the `Load Generator`, `Ingestion`, `RabbitMQ Flow`, and `Processor` sections in Grafana.

## Panels to watch first

- `Services UP`: confirms Prometheus can reach the configured targets.
- `Total HTTP 5xx RPS`: shows whether any service is returning server errors.
- `Load Generator RPS by URI`: confirms load is being generated.
- `Ingestion RPS`: confirms load reaches the ingestion service.
- `Queue Ready Messages by Queue`: shows whether RabbitMQ queues are accumulating backlog.
- `DLQ Current Depth` and `Retry Current Depth`: show failure pressure in the processing flow.
- `JVM Memory Used by Application` and `Process CPU Usage by Application`: quick resource health checks.

## How to read the system flow dashboard

- If `Load Generator RPS` increases but `Ingestion RPS` does not, investigate connectivity or configuration between the load generator and ingestion service.
- If `Ingestion RPS` increases but `telemetry.events` accumulates, the processor is not consuming fast enough or is unavailable.
- If `telemetry.events.retry` grows, there are recoverable processing failures or processor instability.
- If `telemetry.events.dlq` grows, there are poison messages or permanent processing failures.
- If `telemetry.alerts` grows, the alert consumer or dashboard-side visibility flow may be delayed.
- If HTTP `5xx` grows, use `HTTP Error Rate by Application` to identify the service and inspect its logs.
- If JVM memory grows without stabilizing, investigate excessive load, retained objects, or a possible memory leak.

## How to read RabbitMQ Flow

- `RabbitMQ Broker UP` should show both `rabbitmq` and `rabbitmq-detailed-queues` as `UP`.
- `Connections`, `Channels`, `Consumers`, and `Queues` show the broker shape. Sudden drops can indicate disconnected services.
- `Ready Messages by Queue` means messages are waiting for consumption. High `telemetry.events` ready count means the processor is not consuming at the ingestion rate.
- `Unacked Messages by Queue` means messages were delivered to consumers but are still waiting for ACK. High values suggest slow, stuck, or overloaded consumers.
- `Retry Queue Depth` greater than zero means recoverable failures or processor instability.
- `DLQ Queue Depth` greater than zero means poison messages or permanent failures requiring operational action.
- `Broker Message Rates` compares received/routed/delivered rates. If received or routed stays above delivered, backlog will tend to grow.
- `Consumers by Queue` uses per-queue metrics when RabbitMQ exposes them. If only aggregate consumers are available, the panel shows `all-queues`.
- `TODO - metric unavailable - Queues Without Consumers` is marked this way because the current RabbitMQ export exposes aggregate consumers but not per-queue consumer counts.
- `RabbitMQ Memory Usage %` and `RabbitMQ Memory Used` indicate broker memory pressure. Sustained high memory usage is an operational risk.

## RabbitMQ metrics

The root `docker-compose.yml` enables the `rabbitmq_prometheus` plugin through `observability/rabbitmq/enabled_plugins` and exposes port `15692`.

Prometheus scrapes two RabbitMQ endpoints:

- `rabbitmq`: `http://host.docker.internal:15692/metrics`
- `rabbitmq-detailed-queues`: `http://host.docker.internal:15692/metrics/detailed?family=queue_coarse_metrics`

The default `/metrics` endpoint can expose only aggregate queue values depending on the RabbitMQ plugin/version. The system flow dashboard uses fallback queries: detailed per-queue metrics when labels such as `queue` or `name` exist, and aggregate RabbitMQ metrics when per-queue labels are not available.

If RabbitMQ was already running before this change, recreate it so the plugin file and port mapping are applied:

```bash
docker compose up -d --force-recreate rabbitmq
```

## Common problems

- If targets are `DOWN` on Windows or macOS, verify that the services are listening on the expected localhost ports and that Docker can resolve `host.docker.internal`.
- On Linux, the observability compose already includes `extra_hosts: host.docker.internal:host-gateway` for Prometheus.
- If a service target is `DOWN`, open its `/actuator/prometheus` endpoint directly in the browser and verify the port.
- If RabbitMQ metrics are missing, verify `http://localhost:15692/metrics` and recreate the RabbitMQ container so `rabbitmq_prometheus` is enabled.
- Custom telemetry domain metrics are not implemented yet. The current dashboards use default Spring Boot, JVM, HTTP server, system, and RabbitMQ metrics.

## Troubleshooting No Data

1. Open Prometheus at http://localhost:9090 and go to `Status > Targets`.
2. Confirm the Spring Boot jobs and RabbitMQ jobs are `UP`.
3. In Prometheus, run these queries:

   ```promql
   up
   http_server_requests_seconds_count
   jvm_memory_used_bytes
   process_cpu_usage
   rabbitmq_queue_messages_ready
   rabbitmq_detailed_queue_messages_ready
   ```

4. If `application` is missing on a metric, use `job`. The main system flow dashboard is built around `job` because it is guaranteed by Prometheus scrape configuration.
5. If HTTP panels are empty for a service, generate or scrape traffic for that service. Spring Boot creates `http_server_requests_seconds_*` series only after requests are observed.
6. If RabbitMQ per-queue panels are empty but aggregate RabbitMQ metrics exist, verify the `rabbitmq-detailed-queues` target and the `rabbitmq_prometheus` plugin. Some RabbitMQ versions expose aggregate queue metrics on `/metrics` and per-queue metrics on `/metrics/detailed`.
7. If custom metric panels are empty, they are expected until those Micrometer counters/gauges are implemented in code. Those panels are marked with `TODO - Custom metric required`.
8. If `vehicle-ingestion-service` appears as `DOWN` with HTTP `404` on `/actuator/prometheus`, restart or rebuild the ingestion service. A stale runtime can expose `/actuator/health` while still missing the Prometheus endpoint and the configured Actuator exposure.
