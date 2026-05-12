package org.erick.vehicletelemetrydashboard.config;

import org.erick.shared.messaging.RabbitMQConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public DirectExchange telemetryEventsExchange() {
        return new DirectExchange(RabbitMQConstants.Exchanges.TELEMETRY_EVENTS, true, false);
    }

    @Bean
    public DirectExchange telemetryAlertsExchange() {
        return new DirectExchange(RabbitMQConstants.Exchanges.TELEMETRY_ALERTS, true, false);
    }

    @Bean
    public Queue dashboardTelemetryQueue() {
        return new Queue(RabbitMQConstants.Queues.DASHBOARD_TELEMETRY, true);
    }

    @Bean
    public Queue dashboardAlertsQueue() {
        return new Queue(RabbitMQConstants.Queues.DASHBOARD_ALERTS, true);
    }

    @Bean
    public Binding dashboardTelemetryBinding(
            @Qualifier("dashboardTelemetryQueue") Queue queue,
            @Qualifier("telemetryEventsExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(RabbitMQConstants.RoutingKeys.TELEMETRY_EVENTS);
    }

    @Bean
    public Binding dashboardAlertsBinding(
            @Qualifier("dashboardAlertsQueue") Queue queue,
            @Qualifier("telemetryAlertsExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(RabbitMQConstants.RoutingKeys.TELEMETRY_ALERTS);
    }
}
