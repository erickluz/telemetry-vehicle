package org.erick.telemetrydlqservice.config;

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
    public Queue telemetryDLQ() {
        return new Queue(RabbitMQConstants.Queues.TELEMETRY_EVENTS_DLQ, true);
    }

    @Bean
    public Binding telemetryDLQBinding(
            @Qualifier("telemetryDLQ") Queue telemetryDLQ,
            @Qualifier("telemetryEventsExchange") DirectExchange telemetryEventsExchange) {
        return BindingBuilder
                .bind(telemetryDLQ)
                .to(telemetryEventsExchange)
                .with(RabbitMQConstants.RoutingKeys.TELEMETRY_EVENTS_DLQ);
    }
}
