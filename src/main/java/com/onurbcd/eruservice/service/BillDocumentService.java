package com.onurbcd.eruservice.service;

import com.onurbcd.eruservice.constant.Constant;
import com.onurbcd.eruservice.dto.document.MultipartFileDto;
import com.onurbcd.eruservice.enums.ReferenceType;
import com.onurbcd.eruservice.model.BillDocParams;
import com.onurbcd.eruservice.persistency.entity.Document;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class BillDocumentService {

    private final DocumentService documentService;

    public Document createDocument(BillDocParams billDocParams) {
        var path = getPath(billDocParams);
        var name = getName(billDocParams);

        var multipartFileDto = MultipartFileDto
                .builder()
                .path(path)
                .multipartFile(billDocParams.getMultipartFile())
                .name(name)
                .build();

        return documentService.saveOne(multipartFileDto);
    }

    private String getPath(BillDocParams billDocParams) {
        var pathBuilder = new StringBuilder()
                .append(Constant.BILL_DOCUMENT_PATH)
                .append(billDocParams.getPath());

        if (ReferenceType.YEAR != billDocParams.getReferenceType()) {
            var referenceDayPath = billDocParams
                    .getReferenceDayCalendarDate()
                    .format(DateTimeFormatter.ofPattern(Constant.YEAR_PATTERN));

            pathBuilder
                    .append("/")
                    .append(referenceDayPath);
        }

        return pathBuilder.toString();
    }

    private String getName(BillDocParams billDocParams) {
        var referencePart = ReferenceType.YEAR == billDocParams.getReferenceType() ?
                Integer.toString(billDocParams.getReferenceDayCalendarDate().getYear()) :
                org.apache.commons.lang3.StringUtils.leftPad(
                        Integer.toString(billDocParams.getReferenceDayCalendarDate().getMonthValue()),
                        2,
                        "0"
                );

        var extension = StringUtils.getFilenameExtension(billDocParams.getMultipartFile().getOriginalFilename());

        return new StringBuilder()
                .append(referencePart)
                .append("-")
                .append(billDocParams.getDocumentType().getCode())
                .append(".")
                .append(extension != null ? extension : "pdf")
                .toString();
    }
}
