package org.example.services;

import org.example.entity.AppDocument;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface FileService {
    AppDocument processDoc(Message externalMessage);
}
