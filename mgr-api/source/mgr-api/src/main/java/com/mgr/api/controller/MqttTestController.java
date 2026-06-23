package com.mgr.api.controller;

import com.mgr.api.mqtt.MqttPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MqttTestController {
    @Autowired
    private MqttPublisher mqttPublisher;

    @GetMapping("/api/mqtt/chat")
    public String sendChatMessage(@RequestParam String room, @RequestParam String message) {
        String topic = "chat/room_" + room; // VD: chat/room_vip1
        int qos = 1; // Đảm bảo tin nhắn không bị mất
        boolean retain = true;

        mqttPublisher.sendToMqttWithQoS(topic, qos, retain, message);
        return "Đã gửi tin nhắn Chat tới phòng [" + room + "] với QoS " + qos;
    }

    @GetMapping("/api/mqtt/typing")
    public String sendTypingStatus(@RequestParam String room, @RequestParam String user) {
        String topic = "chat/room_" + room + "/typing";
        int qos = 0;
        boolean retain = true;
        String payload = user + " is typing...";
        mqttPublisher.sendToMqttWithQoS(topic, qos, retain, payload);
        return "Đã gửi trạng thái Typing của [" + user + "] tới phòng [" + room + "] với Qos " + qos;
    }

    @GetMapping("/api/mqtt/booking")
    public String sendBooking(@RequestParam String tableId, @RequestParam String customer) {
        String topic = "restaurant/booking/" + tableId;
        int qos = 2;
        boolean retain = true;
        String payload = "Khách " + customer + " đã chốt bàn " + tableId;
        mqttPublisher.sendToMqttWithQoS(topic, qos, retain, payload);
        return "Đã chốt bàn [" +tableId + "] với Qos " + qos;
    }
}
