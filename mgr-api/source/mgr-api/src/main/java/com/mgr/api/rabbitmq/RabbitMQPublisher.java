package com.mgr.api.rabbitmq;

import com.mgr.api.dto.CommandMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RabbitMQPublisher {

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    @Autowired
    private RabbitTemplate rabbitTemplate; // gửi, nhận message với RabbitMQ


    public <T> void sendMessage(String cmd, T data) {
        CommandMessage<T> message = new CommandMessage<>(cmd, data);
        log.info("[RabbitMQ]: {} to Exchange: {}", cmd, exchange);
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
}
