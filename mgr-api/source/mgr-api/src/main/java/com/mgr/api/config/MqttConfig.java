package com.mgr.api.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.MessageHandler;

@Configuration
public class MqttConfig {
    @Value("${mqtt.broker.url}")
    private String brokerUrl;

    @Value("${mqtt.client.username}")
    private String username;

    @Value("${mqtt.client.password}")
    private String password;

    @Bean
    public MqttConnectOptions mqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{brokerUrl});
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(20);
        return options;
    }

    // Khi tạo kết nối mqtt spring sẽ gọi factory
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(mqttConnectOptions()); // chèn cấu hình connection
        return factory;
    }

    // Gửi publish message ra MQTT broker
    @Bean
    public MqttPahoMessageHandler mqttOutbound() {
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler("publisher-client-id", mqttClientFactory());
        messageHandler.setAsync(true); // Gửi nhiều luồng publish sẽ không phải chờ confirm
        messageHandler.setDefaultTopic("default/topic");
        return messageHandler;
    }

    // Kết nối Channel với Handler
    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel") // Đăng kí channel lắng nghe trên channel có "mqttOutboundChannel"
    public MessageHandler mqttOutboundHandler() {
        return mqttOutbound();
    }
    //Bất kỳ message nào được đẩy vào channel "mqttOutboundChannel" → sẽ được MqttPahoMessageHandler xử lý → publish ra MQTT
}
