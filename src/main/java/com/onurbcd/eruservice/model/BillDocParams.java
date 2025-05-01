package com.onurbcd.eruservice.model;

import com.onurbcd.eruservice.enums.DocumentType;
import com.onurbcd.eruservice.enums.ReferenceType;
import lombok.*;

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
