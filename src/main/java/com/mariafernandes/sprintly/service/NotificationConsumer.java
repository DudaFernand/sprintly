package com.mariafernandes.sprintly.service;

import com.mariafernandes.sprintly.config.RabbitMQConfig;
import com.mariafernandes.sprintly.domain.Notification;
import com.mariafernandes.sprintly.domain.User;
import com.mariafernandes.sprintly.dto.NotificationEvent;
import com.mariafernandes.sprintly.repository.NotificationRepository;
import com.mariafernandes.sprintly.repository.UserRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationConsumer(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handle(NotificationEvent event) {
        User recipient = userRepository.findById(event.recipientId())
            .orElse(null);

        if (recipient == null) return;

        Notification notification = new Notification(recipient, event.type(), event.message());
        notificationRepository.save(notification);
    }
}