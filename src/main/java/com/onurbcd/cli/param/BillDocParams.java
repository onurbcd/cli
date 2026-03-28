package com.onurbcd.cli.param;

import com.onurbcd.cli.enums.DocumentType;
import com.onurbcd.cli.enums.ReferenceType;
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
