package com.onurbcd.eruservice.service.validation.impl;

import com.onurbcd.eruservice.model.MultipartFile;
import com.onurbcd.eruservice.service.enums.Error;
import com.onurbcd.eruservice.service.validation.Action;
import com.onurbcd.eruservice.service.validation.DocumentValidationService;
import org.springframework.stereotype.Service;

@Service
public class DocumentValidationServiceImpl implements DocumentValidationService {

    @Override
    public void validate(MultipartFile multipartFile) {
        Action.checkIfNot(multipartFile.isEmpty()).orElseThrow(Error.DOCUMENT_IS_EMPTY);
        Action.checkIfNotBlank(multipartFile.getOriginalFilename()).orElseThrow(Error.DOCUMENT_NAME_IS_BLANK);
        Action.checkIfNotBlank(multipartFile.getContentType()).orElseThrow(Error.DOCUMENT_MIME_TYPE_IS_BLANK);
        Action.checkIfNot(multipartFile.getSize() == 0).orElseThrow(Error.DOCUMENT_SIZE_IS_ZERO);
    }
}
