package com.example.vehicleingestionservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public DirectExchange telemetryExchange() {
        return new DirectExchange("telemetry.events.exchange", true, false);
    }

    @Bean
    public Queue telemetryQueue() {
        return new Queue("telemetry.events", true);
    }

    @Bean
    public Binding telemetryBinding(Queue telemetryQueue, DirectExchange telemetryExchange) {
        return BindingBuilder.bind(telemetryQueue).to(telemetryExchange).with("vehicle.telemetry");
    }
}
