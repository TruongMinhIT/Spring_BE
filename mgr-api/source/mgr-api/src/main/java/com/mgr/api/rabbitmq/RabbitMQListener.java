package com.mgr.api.rabbitmq;

import com.mgr.api.dto.CommandMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RabbitMQListener {

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void consume(CommandMessage<Object> message) {
        log.info("[Đã hứng được Message] - cmd: {}, data: {}", message.getCmd(), message.getData());
    }
}