package org.example.services;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface ProduceService {
    void produceAnswer (SendMessage sendMessage);
}
