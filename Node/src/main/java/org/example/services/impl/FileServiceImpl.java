package org.example.services.impl;

import lombok.extern.log4j.Log4j;
import org.example.dao.AppDocumentDAO;
import org.example.dao.BinaryContentDAO;
import org.example.entity.AppDocument;
import org.example.entity.BinaryContent;
import org.example.exceptions.UploadFileException;
import org.example.services.FileService;
import org.json.JSONObject;
import org.jvnet.hk2.annotations.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Сервис отвечает за получение сообщения с дальнейшей его обработкой и сохранения его в DB
 */

@Log4j
@Service
@Component
public class FileServiceImpl implements FileService {

    @Value("${token}")
    private String token;
    @Value("${service.file_info.uri}")
    private String fileInfoUri;
    @Value("${service.file_storage.uri}")
    private String fileStorageUri;

    private final AppDocumentDAO appDocumentDAO;
    private final BinaryContentDAO binaryContentDAO;

    public FileServiceImpl(AppDocumentDAO appDocumentDAO, BinaryContentDAO binaryContentDAO){
        this.appDocumentDAO = appDocumentDAO;
        this.binaryContentDAO = binaryContentDAO;
    }

    @Override
    public AppDocument processDoc(Message telegramMessage) {
        String fileId = telegramMessage.getDocument().getFileId();
        ResponseEntity<String> response = getFilePath(fileId);
        if (response.getStatusCode() == HttpStatus.OK){
            JSONObject jsonObject = new JSONObject(response.getBody());
            String filePath = String.valueOf(jsonObject.getJSONObject("result").getString("file_path"));
            byte[] fileInByte = downloadFile(filePath);
            BinaryContent transientBinaryContent = BinaryContent.builder().fileAsArrayOfBytes(fileInByte).build();
            BinaryContent persistentBinaryContent = binaryContentDAO.save(transientBinaryContent);
            Document telegramDoc = telegramMessage.getDocument();
            AppDocument transientAppDoc = buildTransientAppDoc(telegramDoc, persistentBinaryContent);
            return appDocumentDAO.save(transientAppDoc);
        }else {
            throw new UploadFileException("Bad response from telegram service" + response);
        }

    }

    private AppDocument buildTransientAppDoc(Document telegramDoc, BinaryContent persistentBinaryContent) {
        return AppDocument.builder().telegramFileId(telegramDoc.getFileId())
                .docName(telegramDoc.getFileName())
                .binaryContent(persistentBinaryContent)
                .mimeType(telegramDoc.getMimeType())
                .fileSize(telegramDoc.getFileSize())
                .build();
    }

    private byte[] downloadFile(String filePath) {
        String fullUri = fileStorageUri.replace("{token}", token).replace("{filePath}", filePath);
        URL urlObj = null;
        try{
            urlObj = new URL(fullUri);
        }catch (MalformedURLException e){
            throw new UploadFileException(e);
        }
        //todo придумай, как оптимизировать

        try (InputStream is = urlObj.openStream()){
            return is.readAllBytes();

        } catch (IOException e) {
            throw new UploadFileException(e); // тут остановился
        }
    }

    private ResponseEntity<String> getFilePath(String fileId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);

        return restTemplate.exchange(
                fileInfoUri,
                HttpMethod.GET,
                request,
                String.class,
                token,
                fileId
        );
    }

}
