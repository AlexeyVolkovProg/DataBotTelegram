package org.example.services;


import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Интерфейс отвечающий за передачу update в RabbitMQ
 */


@Component
public interface UpdateProducer {
    void produce(String rabbitQueue, Update update);

}
