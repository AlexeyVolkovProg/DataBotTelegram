package org.itmo.services;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

/**
 * Интерфейс, отвечающий за получение сообщений от RabbitMQ
 */
public interface AnswerConsumer {
    void consume(SendMessage sendMessage);

}
