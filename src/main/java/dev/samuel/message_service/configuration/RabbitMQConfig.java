package dev.samuel.message_service.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "prolab.exchange";
    public static final String QUEUE_USUARIO = "usuario.queue";
    public static final String ROUTING_KEY_USUARIO = "usuario.mensagem";
    public static final String ROUTING_KEY_USUARIO_ATUALIZADO = "usuario.atualizado";
    public static final String QUEUE_USUARIO_ATUALIZADO = "usuario.atualizado.queue";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue usuarioQueue() {
        return new Queue(QUEUE_USUARIO, true);
    }

    @Bean
    public Queue usuarioAtualizadoQueue() {
        return new Queue(QUEUE_USUARIO_ATUALIZADO, true);
    }

    @Bean
    public Binding binding(Queue usuarioQueue, TopicExchange exchange) {
        return BindingBuilder.bind(usuarioQueue)
                .to(exchange)
                .with(ROUTING_KEY_USUARIO);
    }

    @Bean
    public Binding bindingAtualizado(Queue usuarioAtualizadoQueue, TopicExchange exchange) {
        return BindingBuilder.bind(usuarioAtualizadoQueue)
                .to(exchange)
                .with(ROUTING_KEY_USUARIO_ATUALIZADO);
    }

    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }

}
