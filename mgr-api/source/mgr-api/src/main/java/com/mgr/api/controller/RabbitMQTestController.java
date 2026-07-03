package com.mgr.api.controller;

import com.mgr.api.dto.ApiMessageDto;
import com.mgr.api.dto.CommandMessage;
import com.mgr.api.rabbitmq.RabbitMQPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/message")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class RabbitMQTestController extends ABasicController{
    @Autowired
    private RabbitMQPublisher publisher;

    @PostMapping("/publish")
    public ApiMessageDto<String> sendMessage(@RequestBody CommandMessage<Object> message) {
        publisher.sendMessage(message.getCmd(), message.getData());
        return makeSuccessResponse("Publish message success");
    }
}
