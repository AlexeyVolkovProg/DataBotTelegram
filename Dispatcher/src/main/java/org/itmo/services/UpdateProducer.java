package org.itmo.services;


import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Интерфейс отвечающий за передачу update в RabbitMQ
 */


public interface UpdateProducer {
    void produce(String rabbitQueue, Update update);

}
