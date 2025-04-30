package com.onurbcd.eruservice.service.resource;

import com.onurbcd.eruservice.model.MultipartFile;
import com.onurbcd.eruservice.enums.DocumentType;
import com.onurbcd.eruservice.enums.ReferenceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BillDocParams {

    private String path;

    private LocalDate referenceDayCalendarDate;

    private MultipartFile multipartFile;

    private DocumentType documentType;

    private ReferenceType referenceType;
}
