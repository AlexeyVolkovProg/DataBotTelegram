package org.example.services.impl;

import lombok.extern.log4j.Log4j;
import org.example.services.UpdateProducer;
import org.jvnet.hk2.annotations.Service;
import org.telegram.telegrambots.meta.api.objects.Update;


@Service
@Log4j
public class UpdateProducerImpl implements UpdateProducer {
    @Override
    public void produce(String rabbitQueue, Update update) {
        log.debug(update.getMessage().toString());
    }
}
