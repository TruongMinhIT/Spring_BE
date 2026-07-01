package com.mgr.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;

@Configuration
public class MqttSubscriberConfig {
    private final MqttPahoClientFactory mqttPahoClientFactory;

    public MqttSubscriberConfig(MqttPahoClientFactory mqttPahoClientFactory) {
        this.mqttPahoClientFactory = mqttPahoClientFactory;
    }

    //	Nhận (subscribe) message từ MQTT Broker
    @Bean
    public MqttPahoMessageDrivenChannelAdapter inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(
                "subscriber-client-id", mqttPahoClientFactory,
                "chat/#", "restaurant/#");
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannelName("mqttInboundChannel");
        return adapter;
    }

    @ServiceActivator(inputChannel = "mqttInboundChannel")
    public void handleMessage(Message<?> message) {
        // Lấy Topic
        String topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();

        // Lấy nội dung tin nhắn
        String payload = message.getPayload().toString();

        System.out.println("📥 Nhận tin nhắn từ topic [" + topic + "]: " + payload);

        // Tại đây bạn có thể gọi các Service (@Autowired) để lưu vào Database
    }
}
