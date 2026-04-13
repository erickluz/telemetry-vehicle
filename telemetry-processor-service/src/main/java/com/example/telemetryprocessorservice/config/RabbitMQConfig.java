package com.example.telemetryprocessorservice.config;

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
    public Queue telemetryQueue() {
        return new Queue("telemetry.events", true);
    }

    @Bean
    public Queue alertQueue() {
        return new Queue("telemetry.alerts", true);
    }

    @Bean
    public Binding telemetryBinding(@Qualifier("telemetryQueue") Queue telemetryQueue, @Qualifier("telemetryEventsExchange") DirectExchange telemetryEventsExchange) {
        return BindingBuilder.bind(telemetryQueue).to(telemetryEventsExchange).with("telemetry.events");
    }

    @Bean
    public Binding alertBinding(@Qualifier("alertQueue") Queue alertQueue, @Qualifier("telemetryAlertsExchange") DirectExchange telemetryAlertsExchange) {
        return BindingBuilder.bind(alertQueue).to(telemetryAlertsExchange).with("telemetry.alerts");
    }
}
