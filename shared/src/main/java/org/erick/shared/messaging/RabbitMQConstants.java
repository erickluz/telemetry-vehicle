package org.erick.shared.messaging;

public final class RabbitMQConstants {

    private RabbitMQConstants() {
    }

    public static final class Exchanges {
        public static final String TELEMETRY_EVENTS = "telemetry.events.exchange";
        public static final String TELEMETRY_ALERTS = "telemetry.alerts.exchange";

        private Exchanges() {
        }
    }

    public static final class Queues {
        public static final String TELEMETRY_EVENTS = "telemetry.events";
        public static final String TELEMETRY_EVENTS_RETRY = "telemetry.events.retry";
        public static final String TELEMETRY_EVENTS_DLQ = "telemetry.events.dlq";
        public static final String TELEMETRY_ALERTS = "telemetry.alerts";
        public static final String DASHBOARD_TELEMETRY = "vehicle.dashboard.telemetry";
        public static final String DASHBOARD_ALERTS = "vehicle.dashboard.alerts";

        private Queues() {
        }
    }

    public static final class RoutingKeys {
        public static final String TELEMETRY_EVENTS = "telemetry.events";
        public static final String TELEMETRY_EVENTS_RETRY = "telemetry.events.retry";
        public static final String TELEMETRY_EVENTS_DLQ = "telemetry.events.dlq";
        public static final String TELEMETRY_ALERTS = "telemetry.alerts";
        public static final String TELEMETRY_EVENTS_ALERTS = "telemetry.events.alerts";

        private RoutingKeys() {
        }
    }

    public static final class Headers {
        public static final String REPROCESSED = "x-reprocessed";
        public static final String REPROCESS_SOURCE = "x-reprocess-source";
        public static final String DLQ_RECORD_ID = "x-dlq-record-id";
        public static final String REPROCESS_COUNT = "x-reprocess-count";

        private Headers() {
        }
    }
}
