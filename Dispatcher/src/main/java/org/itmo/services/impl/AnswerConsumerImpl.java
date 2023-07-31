package org.itmo.services.impl;

import org.itmo.controller.UpdateController;
import org.itmo.services.AnswerConsumer;
import org.itmo.services.UpdateProducer;
import org.jvnet.hk2.annotations.Service;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static org.itmo.model.RabbitQueue.ANSWER_MESSAGE;

@Service
@Component
public class AnswerConsumerImpl implements AnswerConsumer {
    private final UpdateController updateController;
    public AnswerConsumerImpl(UpdateController updateController){
        this.updateController = updateController;
    }

    @Override
    @RabbitListener(queues = ANSWER_MESSAGE)
    public void consume(SendMessage sendMessage) {
        updateController.setView(sendMessage);
    }



}
