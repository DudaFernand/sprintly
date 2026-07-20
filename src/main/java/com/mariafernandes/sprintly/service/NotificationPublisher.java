package com.mariafernandes.sprintly.service;

import com.mariafernandes.sprintly.config.RabbitMQConfig;
import com.mariafernandes.sprintly.dto.NotificationEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    public NotificationPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(Long recipientId, String type, String message) {
        NotificationEvent event = new NotificationEvent(recipientId, type, message);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "notification." + type, event);
    }
}