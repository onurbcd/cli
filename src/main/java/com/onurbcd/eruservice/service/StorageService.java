package com.onurbcd.eruservice.service;

import com.onurbcd.eruservice.model.MultipartFile;
import com.onurbcd.eruservice.persistency.entity.Document;

public interface StorageService {

    void saveFile(Document document, MultipartFile multipartFile);

    void deleteFile(Document document);

    byte[] getFile(Document document);
}
