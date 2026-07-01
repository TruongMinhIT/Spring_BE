package com.mgr.api.mqtt;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@MessagingGateway(defaultRequestChannel = "mqttOutboundChannel") //Tạo interface gateway để gọi publish dễ dàng
@Component
public interface MqttPublisher {
    // Đẩy message vào channel mqttOutboundChannel
    void sendToMqtt(@Header(MqttHeaders.TOPIC) String topic, String data);

    void sendToMqttWithQoS(@Header(MqttHeaders.TOPIC) String topic,
                           @Header(MqttHeaders.QOS) Integer qos,
                           @Header(MqttHeaders.RETAINED) Boolean retained,
                           String data);
}
