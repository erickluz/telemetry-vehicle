package org.erick.notificationservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public DirectExchange alertsExchange() {
        return new DirectExchange("telemetry.alerts.exchange", true, false);
    }

    @Bean
    public Queue alertsQueue() {
        return new Queue("telemetry.alerts", true);
    }

    @Bean
    public Binding alertsBinding(Queue alertsQueue, DirectExchange alertsExchange) {
        return BindingBuilder.bind(alertsQueue).to(alertsExchange).with("telemetry.alerts");
    }
}
