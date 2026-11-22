package com.onurbcd.cli.mapper;

import com.onurbcd.cli.dto.document.DocumentDto;
import com.onurbcd.cli.persistency.entity.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.onurbcd.cli.TestConstants.DOCUMENT_ID;
import static org.assertj.core.api.Assertions.assertThat;

class DocumentToDtoMapperTest {

    private final DocumentToDtoMapperImpl mapper = new DocumentToDtoMapperImpl();

    private static Document createDocument() {
        var document = new Document();
        document.setId(DOCUMENT_ID);
        document.setName("doc.pdf");
        document.setPath("balance/doc.pdf");
        document.setMimeType("application/pdf");
        document.setSize(10L);
        document.setHash("hash");
        return document;
    }

    @Nested
    @DisplayName("apply")
    class Apply {

        @Test
        @DisplayName("when document is complete, should map the fields and leave the storage path empty")
        void givenFullDocument_whenApply_shouldMapEveryField() {
            var document = createDocument();

            var result = mapper.apply(document);

            var expected = new DocumentDto(DOCUMENT_ID, "doc.pdf", "balance/doc.pdf", "application/pdf", 10L, "hash");
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);
            assertThat(result.getStoragePath()).isNull();
            assertThat(document).usingRecursiveComparison().isEqualTo(createDocument());
        }

        @Test
        @DisplayName("when document is null, should return an empty dto")
        void givenNullDocument_whenApply_shouldReturnEmptyDto() {
            var result = mapper.apply(null);

            assertThat(result).usingRecursiveComparison().isEqualTo(new DocumentDto());
        }
    }
}
