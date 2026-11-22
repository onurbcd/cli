package com.onurbcd.cli.service;

import com.onurbcd.cli.dto.document.MultipartFileDto;
import com.onurbcd.cli.enums.DocumentType;
import com.onurbcd.cli.enums.ReferenceType;
import com.onurbcd.cli.param.BillDocParams;
import com.onurbcd.cli.param.MultipartFile;
import com.onurbcd.cli.persistency.entity.Document;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillDocumentServiceTest {

    @Mock
    private DocumentService documentService;
    @Captor
    private ArgumentCaptor<MultipartFileDto> dtoCaptor;
    @InjectMocks
    private BillDocumentService service;

    @ParameterizedTest(name = "[{index}] {0} {1} {2} -> {3} / {4}")
    @CsvSource(nullValues = "NULL", value = {
            "YEAR,  2024-03-15, invoice.pdf, bill/water,              2024-boleto.pdf",
            "YEAR,  2024-12-01, scan.png,    bill/water,              2024-boleto.png",
            "MONTH, 2024-03-15, invoice.pdf, bill/water/2024,         03-boleto.pdf",
            "MONTH, 2024-11-15, invoice.jpg, bill/water/2024,         11-boleto.jpg",
            "DAY,   2023-01-05, invoice,     bill/water/2023,         01-boleto.pdf",
            "MONTH, 2024-03-15, NULL,        bill/water/2024,         03-boleto.pdf"
    })
    void givenParams_whenCreateDocument_shouldSaveOneWithReferencePathAndName(ReferenceType referenceType,
                                                                             LocalDate referenceDate,
                                                                             String originalFilename,
                                                                             String expectedPathWithBase,
                                                                             String expectedName) {
        var upload = MultipartFile.builder().originalFilename(originalFilename).contentType("application/pdf")
                .size(10L).build();
        var params = BillDocParams.builder().path("water").referenceDayCalendarDate(referenceDate)
                .multipartFile(upload).documentType(DocumentType.BILLET).referenceType(referenceType).build();
        var saved = new Document();
        when(documentService.saveOne(any(MultipartFileDto.class))).thenReturn(saved);

        var result = service.createDocument(params);

        assertThat(result).isSameAs(saved);
        verify(documentService).saveOne(dtoCaptor.capture());
        var dto = dtoCaptor.getValue();
        assertThat(dto.getPath()).isEqualTo(expectedPathWithBase);
        assertThat(dto.getName()).isEqualTo(expectedName);
        assertThat(dto.getMultipartFile()).isSameAs(upload);
        assertThat(dto.getMultipartFiles()).isNull();
        verifyNoMoreInteractions(documentService);
    }
}
