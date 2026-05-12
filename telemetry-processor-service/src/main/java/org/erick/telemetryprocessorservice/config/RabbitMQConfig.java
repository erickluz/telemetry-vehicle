package org.erick.telemetryprocessorservice.config;

import org.erick.shared.messaging.RabbitMQConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    private static final int TTL = 10000;

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
     }

    @Bean
    public DirectExchange telemetryEventsExchange() {
        return new DirectExchange(RabbitMQConstants.Exchanges.TELEMETRY_EVENTS, true, false);
    }

    @Bean
    public Queue telemetryQueue() {
        return new Queue(RabbitMQConstants.Queues.TELEMETRY_EVENTS, true);
    }

    @Bean
    public Queue telemetryDLQ() {
        return new Queue(RabbitMQConstants.Queues.TELEMETRY_EVENTS_DLQ, true);
    }

    @Bean
    public Queue telemetryRetryQueue() {
        return QueueBuilder.durable(RabbitMQConstants.Queues.TELEMETRY_EVENTS_RETRY)
                .ttl(TTL)
                .withArgument("x-dead-letter-exchange", RabbitMQConstants.Exchanges.TELEMETRY_EVENTS)
                .withArgument("x-dead-letter-routing-key", RabbitMQConstants.RoutingKeys.TELEMETRY_EVENTS)
                .build();
    }

    @Bean
    public Binding telemetryBinding(@Qualifier("telemetryQueue") Queue telemetryQueue, @Qualifier("telemetryEventsExchange") DirectExchange telemetryEventsExchange) {
        return BindingBuilder
            .bind(telemetryQueue)
            .to(telemetryEventsExchange)
            .with(RabbitMQConstants.RoutingKeys.TELEMETRY_EVENTS);
    }

    @Bean
    public Binding telemetryRetryBinding(@Qualifier("telemetryRetryQueue") Queue telemetryRetryQueue, @Qualifier("telemetryEventsExchange") DirectExchange telemetryEventsExchange) {
        return BindingBuilder
            .bind(telemetryRetryQueue)
            .to(telemetryEventsExchange)
            .with(RabbitMQConstants.RoutingKeys.TELEMETRY_EVENTS_RETRY);
    }

    @Bean
    public Binding telemetryDLQBinding(@Qualifier("telemetryDLQ") Queue telemetryDLQ, @Qualifier("telemetryEventsExchange") DirectExchange telemetryEventsExchange) {
        return BindingBuilder
            .bind(telemetryDLQ)
            .to(telemetryEventsExchange)
            .with(RabbitMQConstants.RoutingKeys.TELEMETRY_EVENTS_DLQ);
    }

    @Bean
    public DirectExchange telemetryAlertsExchange() {
        return new DirectExchange(RabbitMQConstants.Exchanges.TELEMETRY_ALERTS, true, false);
    }
    
}
