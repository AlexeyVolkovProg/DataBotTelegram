package org.example.services.impl;

import lombok.extern.log4j.Log4j;
import org.example.dao.AppUserDAO;
import org.example.dao.RawDataDAO;
import org.example.entity.AppDocument;
import org.example.entity.AppUser;
import org.example.entity.RawData;
import org.example.services.FileService;
import org.example.services.MainService;
import org.example.services.ProduceService;
import org.example.services.enums.ServiceCommands;
import org.glassfish.jersey.internal.inject.UpdaterException;
import org.jvnet.hk2.annotations.Service;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static org.example.entity.enums.UserState.BASIC_STATE;
import static org.example.entity.enums.UserState.WAIT_FOR_EMAIL_STATE;
import static org.example.services.enums.ServiceCommands.*;


/**
 * Главные сервис по обработке данных
 */
@Service
@Log4j
@Component
public class MainServiceImpl implements MainService {
    private final RawDataDAO rawDataDAO;
    private final ProduceService produceService;
    private final AppUserDAO appUserDAO;

    private final FileService fileService;

    public  MainServiceImpl(RawDataDAO rawDataDAO, ProduceService produceService, AppUserDAO appUserDAO, FileService fileService){
        this.produceService = produceService;
        this.rawDataDAO = rawDataDAO;
        this.appUserDAO = appUserDAO;
        this.fileService = fileService;
    }


    /**
     * Обработка текстовых сообщений
     * @param update данные
     */

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var userState = appUser.getState();
        var text = update.getMessage().getText();
        var output = "";

        var serviceCommand = ServiceCommands.fromValue(text);
        if (CANCEL.equals(serviceCommand)){
            output = cancelProcess(appUser);
        }else if (BASIC_STATE.equals(userState)){
            output = processServiceCommand(appUser, serviceCommand);
        }else if (WAIT_FOR_EMAIL_STATE.equals(userState)){
            //todo реализуй регистрацию через email
        }else{
            output = "Произошла ошибка. Введите /cancel и попробуйте снова.";
        }
        var chatId = update.getMessage().getChatId();
        sendAnswer(output, chatId);
    }


    /**
     * Обработка документов
     * @param update
     */
    @Override
    public void processDocMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();
        if (isNotAllowToSendContent(chatId, appUser)){
            return;
        }
        try {
            AppDocument doc = fileService.processDoc(update.getMessage());
            var answer = "Документ успешно обработан! Держи ссылку: @first_link";
            sendAnswer(answer, chatId);
        }catch (UpdaterException ex){
            log.error(ex);
            String error = "К сожалению загрузка файла не удалась. Повторите попытку позже.";
            sendAnswer(error, chatId);
        }
    }

    private boolean isNotAllowToSendContent(Long chatId, AppUser appUser) {
        var userState = appUser.getState();
        if (!appUser.getIsActive()){
            var error = "Зарегистрируйтесь или активируйте свою учетную запись для загрузки контента";
            sendAnswer(error, chatId);
            return true;
        }else if (!BASIC_STATE.equals(userState)){
            var error = "Отмените текущую команду при помощи команды /cancel для отправки сообщений";
            sendAnswer(error, chatId);
            return true;
        }
        return false;
    }

    /**
     * Обработка фото
     * @param update
     */

    @Override
    public void processPhotoMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();
        if (isNotAllowToSendContent(chatId, appUser)){
            return;
        }
        // todo добавить сохранение документа
        var answer = "Документ успешно обработан! Держи ссылку: @first_link";
        sendAnswer(answer, chatId);
    }


    /**
     * Отправка ответа пользователю
     * @param output сообщение
     * @param chatId куда отправляется
     */
    private void sendAnswer(String output, Long chatId) {
        var sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        produceService.produceAnswer(sendMessage);
    }

    /**
     * Определяет какая команда перед нами и далее запускает нужные нам методы
     * @param appUser
     * @param text
     * @return
     */
    private String processServiceCommand(AppUser appUser, ServiceCommands serviceCommands) {
        if(REGISTRATION.equals(serviceCommands)){
            return "Команда регистрации временно недоступна";
        }else if (HELP.equals(serviceCommands)){
            return help();
        }else if (START.equals(serviceCommands)){
            return "Приветствую. Чтобы посмотреть все доступные команды введите /help";
        }else{
            return "Неизвестная команда. Чтобы посмотреть все доступные команды введите /help";
        }
    }

    private String help(){
        return  "Список доступных команд:\n"
                + "/cancel - отмена выполнения текущей команды;\n"
                + "/registration - регистрация пользователя.";
    }

    private String cancelProcess(AppUser appUser) {
        appUser.setState(BASIC_STATE);
        appUserDAO.save(appUser);
        return "Команда успешно отменена!";
    }

    /**
     * Добавление нового пользователя в базу данных
     * @param update сообщение, по которому мы найдем юзера, от которого пришли данные
     * @return нового юзера(или уже существующего)
     */

    private AppUser findOrSaveAppUser(Update update){
        User telegramUser = update.getMessage().getFrom();
        AppUser persistentAppUser = appUserDAO.findAppUserByTelegramUserId(telegramUser.getId());
        if (persistentAppUser == null){
            AppUser transientAppUser = AppUser.builder().telegramUserId(telegramUser.getId()).
                    username(telegramUser.getUserName()).firstName(telegramUser.getFirstName()).lastName(telegramUser.getLastName()).
                    isActive(true).state(BASIC_STATE).build(); //todo сделать нормальную проверку на то готов ли пользователь
            return appUserDAO.save(transientAppUser);
        }
        return persistentAppUser;
    }

    /**
     * Сохраняет данные, которые содержатся в сообщениях в таблицу RawData
     * @param update данные
     */

    public void saveRawData(Update update){
        RawData rawData = RawData.builder().event(update).build();
        rawDataDAO.save(rawData);
    }
}
