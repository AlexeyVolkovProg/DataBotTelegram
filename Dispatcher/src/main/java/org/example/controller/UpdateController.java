package org.example.controller;

import lombok.extern.log4j.Log4j;
import org.example.utils.MessageUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Класс отвечающий за распределение сообщений
 */

@Component
@Log4j
public class UpdateController {
    TelegramBot telegramBot;
    MessageUtils messageUtils;

    public void registerBot(TelegramBot bot) {
        telegramBot = bot;
    }

    /**
     * Метод, занимающийся проверкой содержания входящих сообщений
     */
    public void processUpdate(Update update) {
        if (update == null) {
            log.error("Received message is null");
        }
        if (update.getMessage() != null) {
            distributeMessageByType(update);
        } else {
            log.error("Received unsupported message type" + update);
        }

    }

    /**
     * Метод, проверяющий тип входящего сообщения
     */
    public void distributeMessageByType(Update update) {
        var message = update.getMessage();
        if (message.getText() != null) {
            processTextMessage(update);
        }else if (message.getDocument() != null){
            processDocMessage(update);
        }else if (message.getPhoto() != null){
            processPhotoMessage(update);
        }else{
            setUnsupportedTypeMessage(update);
        }


    }


    public void processTextMessage(Update message) {

    }

    public void processPhotoMessage(Update message) {

    }

    public void processDocMessage(Update message){

    }

    /**
     * Метод, отвечающий за обработку неподдерживаемых сообщений
     * @param message
     */
    public void setUnsupportedTypeMessage(Update message){
        var sendMessage = messageUtils.generateSendMessageWithText(message, "Неподдерживаемый тип сообщений!");
        setView(sendMessage);
    }

    private void setView(SendMessage sendMessage) {
        telegramBot.sendAnswerMessage(sendMessage);
    }


}
