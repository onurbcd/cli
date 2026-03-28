package com.onurbcd.cli.service;

import com.onurbcd.cli.util.Constant;
import com.onurbcd.cli.dto.balance.BalanceSaveDto;
import com.onurbcd.cli.dto.document.MultipartFileDto;
import com.onurbcd.cli.param.CreateDocument;
import com.onurbcd.cli.param.MultipartFile;
import com.onurbcd.cli.persistency.entity.Document;
import com.onurbcd.cli.persistency.repository.BalanceRepository;
import com.onurbcd.cli.util.CollectionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class BalanceDocumentService {

    private final DocumentService documentService;
    private final BalanceRepository balanceRepository;

    public CreateDocument createDocuments(BalanceSaveDto saveDto, @Nullable MultipartFile[] multipartFiles, @Nullable UUID id) {
        var currentDocuments = getCurrentDocuments(id);
        var newDocuments = saveDocuments(saveDto, multipartFiles);
        var updateDocuments = getDocumentsToUpdate(currentDocuments, saveDto);
        var saveDocuments = Stream.concat(newDocuments.stream(), updateDocuments.stream()).collect(Collectors.toSet());
        var deleteDocuments = getDocumentsToDelete(currentDocuments, saveDto);
        return CreateDocument.builder().saveDocuments(saveDocuments).deleteDocuments(deleteDocuments).build();
    }

    public void deleteDocuments(Set<Document> documents) {
        if (CollectionUtil.isEmpty(documents)) {
            return;
        }

        documentService.delete(documents);
    }

    private Set<Document> saveDocuments(BalanceSaveDto saveDto, @Nullable MultipartFile[] multipartFiles) {
        if (multipartFiles == null || multipartFiles.length < 1) {
            return Collections.emptySet();
        }

        var path = getPath(saveDto.getDayCalendarDate());
        var multipartFileDto = MultipartFileDto.builder().path(path).multipartFiles(multipartFiles).build();
        return documentService.save(multipartFileDto);
    }

    private String getPath(LocalDate dayCalendarDate) {
        return Constant.BALANCE_DOCUMENT_PATH +
                dayCalendarDate.format(DateTimeFormatter.ofPattern(Constant.BALANCE_DOCUMENT_PATH_PATTERN));
    }

    private Set<Document> getCurrentDocuments(@Nullable UUID id) {
        return Optional
                .ofNullable(id)
                .map(balanceRepository::getDocuments)
                .filter(CollectionUtil::isNotEmpty)
                .orElse(Collections.emptySet());
    }

    private Set<Document> getDocumentsToUpdate(Set<Document> currentDocuments, BalanceSaveDto saveDto) {
        return currentDocuments
                .stream()
                .filter(documentPredicate(saveDto))
                .collect(Collectors.toSet());
    }

    private Set<Document> getDocumentsToDelete(Set<Document> currentDocuments, BalanceSaveDto saveDto) {
        return currentDocuments
                .stream()
                .filter(documentPredicate(saveDto).negate())
                .collect(Collectors.toSet());
    }

    private static Predicate<Document> documentPredicate(BalanceSaveDto saveDto) {
        return doc -> Optional
                .ofNullable(saveDto.getDocumentsIds())
                .orElse(Collections.emptySet())
                .contains(doc.getId());
    }
}
