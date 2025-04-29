package com.onurbcd.eruservice.service.impl;

import com.onurbcd.eruservice.model.MultipartFile;
import com.onurbcd.eruservice.dto.document.DocumentDto;
import com.onurbcd.eruservice.dto.document.FileDto;
import com.onurbcd.eruservice.dto.document.MultipartFileDto;
import com.onurbcd.eruservice.persistency.entity.Document;
import com.onurbcd.eruservice.persistency.repository.DocumentRepository;
import com.onurbcd.eruservice.service.DocumentService;
import com.onurbcd.eruservice.service.StorageService;
import com.onurbcd.eruservice.service.enums.Error;
import com.onurbcd.eruservice.service.exception.ApiException;
import com.onurbcd.eruservice.service.mapper.DocumentToDtoMapper;
import com.onurbcd.eruservice.service.validation.Action;
import com.onurbcd.eruservice.service.validation.DocumentValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private static final int RADIX = 16;

    private final DocumentRepository repository;

    private final StorageService storageService;

    private final DocumentToDtoMapper toDtoMapper;

    private final DocumentValidationService validationService;

    @Override
    public Set<Document> save(MultipartFileDto dto) {
        var documents = new ArrayList<Document>();

        for (var multipartFile : dto.getMultipartFiles()) {
            validationService.validate(multipartFile);
            var document = create(multipartFile, dto.getPath());
            documents.add(document);
        }

        return Set.copyOf(repository.saveAll(documents));
    }

    @Override
    public Document saveOne(MultipartFileDto dto) {
        validationService.validate(dto.getMultipartFile());
        var document = create(dto.getMultipartFile(), dto.getPath(), dto.getName());
        return repository.save(document);
    }

    @Override
    public Page<DocumentDto> getAll(Pageable pageable) {
        return repository.findAll(pageable).map(toDtoMapper);
    }

    @Override
    public void delete(Set<Document> documents) {
        for (var document : documents) {
            storageService.deleteFile(document);
            repository.deleteUsingId(Objects.requireNonNull(document.getId()));
        }
    }

    @Override
    public FileDto getFile(UUID id) {
        var document = repository.findById(id).orElse(null);
        Action.checkIfNotNull(document).orElseThrowNotFound(id);
        Objects.requireNonNull(document);

        var file = storageService.getFile(document);

        /*var headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + document.getName() + "\"");
        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        headers.add(HttpHeaders.PRAGMA, "no-cache");
        headers.add(HttpHeaders.EXPIRES, "0");*/

        return FileDto
                .builder()
                // .headers(headers)
                .contentLength(document.getSize())
                // .contentType(MediaType.parseMediaType(document.getMimeType()))
                .resource(new ByteArrayResource(file))
                .build();
    }

    private Document create(MultipartFile multipartFile, String path) {
        return create(multipartFile, path, Objects.requireNonNull(multipartFile.getOriginalFilename()));
    }

    private Document create(MultipartFile multipartFile, String path, String name) {
        var document = new Document();
        document.setName(name);
        document.setPath(path);
        document.setMimeType(Objects.requireNonNull(multipartFile.getContentType()));
        document.setSize(multipartFile.getSize());
        var hash = generateHash(document);
        document.setHash(hash);
        storageService.saveFile(document, multipartFile);
        return document;
    }

    private String generateHash(Document document) {
        try {
            var transformedName = document.getName() + document.getPath() + document.getMimeType() +
                    document.getSize() + new Date().getTime();

            var messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(transformedName.getBytes(StandardCharsets.UTF_8));
            return new BigInteger(1, messageDigest.digest()).toString(RADIX);
        } catch (NoSuchAlgorithmException e) {
            throw new ApiException(Error.DOCUMENT_GENERATE_HASH, e.toString());
        }
    }
}
