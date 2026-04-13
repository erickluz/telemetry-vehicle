package org.erick.vehicletelemetrydashboard.config;

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
        return new DirectExchange("telemetry.events.exchange", true, false);
    }

    @Bean
    public DirectExchange telemetryAlertsExchange() {
        return new DirectExchange("telemetry.alerts.exchange", true, false);
    }

    @Bean
    public Queue dashboardTelemetryQueue() {
        return new Queue("vehicle.dashboard.telemetry", true);
    }

    @Bean
    public Queue dashboardAlertsQueue() {
        return new Queue("vehicle.dashboard.alerts", true);
    }

    @Bean
    public Binding dashboardTelemetryBinding(
            @Qualifier("dashboardTelemetryQueue") Queue queue,
            @Qualifier("telemetryEventsExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("telemetry.events");
    }

    @Bean
    public Binding dashboardAlertsBinding(
            @Qualifier("dashboardAlertsQueue") Queue queue,
            @Qualifier("telemetryAlertsExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("telemetry.alerts");
    }
}
