package com.onurbcd.eruservice.service;

import com.onurbcd.eruservice.model.MultipartFile;
import com.onurbcd.eruservice.dto.balance.BalanceSaveDto;
import com.onurbcd.eruservice.persistency.entity.Document;
import com.onurbcd.eruservice.model.CreateDocument;
import org.springframework.lang.Nullable;

import java.util.Set;
import java.util.UUID;

public interface BalanceDocumentService {

    CreateDocument createDocuments(BalanceSaveDto saveDto, @Nullable MultipartFile[] multipartFiles, @Nullable UUID id);

    void deleteDocuments(Set<Document> documents);
}
