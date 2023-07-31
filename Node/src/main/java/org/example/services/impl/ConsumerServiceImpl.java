package org.example.services.impl;

import lombok.extern.log4j.Log4j;
import org.example.services.ConsumerService;
import org.example.services.MainService;
import org.example.services.ProduceService;
import org.jvnet.hk2.annotations.Service;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.itmo.model.RabbitQueue.*;

/**
 * Сервис, использующийся для получения в Node данных из RabbitMQ
 */
@Service
@Component
@Log4j
public class ConsumerServiceImpl implements ConsumerService {
    ProduceService produceService;
    MainService mainService;

    public ConsumerServiceImpl(ProduceService producerService, MainService mainService) {
        this.mainService = mainService;
    }

    /**
     * Принимаем текстовые сообщения из соответствующей очереди
     * @param update данные
     */

    @Override
    @RabbitListener(queues = TEXT_MESSAGE_UPDATE)
    public void consumeTextMessage(Update update) {
        log.debug("Node: Text message is received");
        mainService.processTextMessage(update);
    }

    /**
     * Принимаем документы из соответствующей очереди
     * @param update данные
     */
    @Override
    @RabbitListener(queues = DOC_MESSAGE_UPDATE)
    public void consumeDocMessage(Update update) {
        log.debug("Node: Doc message is received");
        mainService.processDocMessage(update);
    }

    /**
     * Принимаем фото из соответствующей очереди
     * @param update данные
     */

    @Override
    @RabbitListener(queues = PHOTO_MESSAGE_UPDATE)
    public void consumePhotoMessage(Update update) {
        log.debug("Node: Photo message is received");
        mainService.processPhotoMessage(update);
    }
}
