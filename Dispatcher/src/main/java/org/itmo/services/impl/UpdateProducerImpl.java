package org.itmo.services.impl;

import lombok.extern.log4j.Log4j;
import org.itmo.services.UpdateProducer;
import org.jvnet.hk2.annotations.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;


@Service
@Component
@Log4j
public class UpdateProducerImpl implements UpdateProducer {

    private final RabbitTemplate rabbitTemplate;

    public UpdateProducerImpl(RabbitTemplate rabbitTemplate){
        this.rabbitTemplate = rabbitTemplate;
    }
    @Override
    public void produce(String rabbitQueue, Update update) {
        log.debug(update.getMessage().toString());
        rabbitTemplate.convertAndSend(rabbitQueue, update);
    }
}
