package org.example.services.impl;

import org.example.services.ProduceService;
import org.jvnet.hk2.annotations.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static org.itmo.model.RabbitQueue.ANSWER_MESSAGE;

/**
 * Сервис, использующийся для добавления ответных сообщений из Node в очередь RabbitMq
 */
@Service
@Component
public class ProduceServiceImpl implements ProduceService {
    private final RabbitTemplate rabbitTemplate;

    public ProduceServiceImpl(RabbitTemplate rabbitTemplate){
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void produceAnswer(SendMessage sendMessage) {
        rabbitTemplate.convertAndSend(ANSWER_MESSAGE, sendMessage);
    }
}
