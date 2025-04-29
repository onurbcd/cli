package com.onurbcd.eruservice.service.validation;

import com.onurbcd.eruservice.model.MultipartFile;

public interface DocumentValidationService {

    void validate(MultipartFile multipartFile);
}
