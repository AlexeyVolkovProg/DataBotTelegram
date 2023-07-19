package org.example;

import org.example.controller.TelegramBot;
import org.example.controller.UpdateController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication

public class DispatcherApplication {
    public static void main(String args[]) {
        SpringApplication.run(DispatcherApplication.class);
//        UpdateController updateController = new UpdateController();
//        TelegramBot telegramBot = new TelegramBot(updateController);
    }
}
