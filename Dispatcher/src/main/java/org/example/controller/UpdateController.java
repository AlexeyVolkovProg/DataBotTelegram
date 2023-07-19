package org.example.controller;

import lombok.extern.log4j.Log4j;
import org.example.services.UpdateProducer;
import org.example.services.impl.UpdateProducerImpl;
import org.example.utils.MessageUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.itmo.model.RabbitQueue.*;

/**
 * Класс отвечающий за распределение сообщений
 * Контроллер связанный с главным классом бота, TelegramBot
 * Реализует взаимодействие с сервисами
 */

@Component
@Log4j
public class UpdateController {
    private TelegramBot telegramBot;
    private MessageUtils messageUtils;
    private UpdateProducer updateProducer;


    /**
     * Метод, соединяющий контроллер сообщений с главным классом бота
     * @param bot бот, с которым соединяемся
     */
    public void registerBot(TelegramBot bot) {
        telegramBot = bot;
        messageUtils = new MessageUtils();
        updateProducer = new UpdateProducerImpl();
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

    private void setFileIsReceivedView(Update update) {
        var sendMessage = messageUtils.generateSendMessageWithText(update, "Ваши данные приняты в обработку! Производится добавление в хранилище...");
        setView(sendMessage);
    }


    /**
     * Производим отправку данным в RabbitMQ
     * @param update содержит отправляемые данные
     */


    public void processTextMessage(Update update) {
        updateProducer.produce(TEXT_MESSAGE_UPDATE, update);
        setFileIsReceivedView(update);
    }



    /**
     * Производим отправку данным в RabbitMQ
     * @param update содержит отправляемые данные
     */
    public void processPhotoMessage(Update update) {
        updateProducer.produce(PHOTO_MESSAGE_UPDATE, update);

    }

    /**
     * Производим отправку данным в RabbitMQ
     * @param update содержит отправляемые данные
     */
    public void processDocMessage(Update update){
        updateProducer.produce(DOC_MESSAGE_UPDATE, update);

    }

    /**
     * Метод, отвечающий за обработку неподдерживаемых сообщений
     * @param update содержит отправляемые данные
     */
    public void setUnsupportedTypeMessage(Update update){
        var sendMessage = messageUtils.generateSendMessageWithText(update, "Неподдерживаемый тип сообщений!");
        setView(sendMessage);
    }


    private void setView(SendMessage sendMessage) {
        telegramBot.sendAnswerMessage(sendMessage);
    }


}
