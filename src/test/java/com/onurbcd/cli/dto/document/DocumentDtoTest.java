package com.onurbcd.cli.dto.document;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.onurbcd.cli.TestConstants.DOCUMENT_ID;
import static com.onurbcd.cli.TestFactory.createDocumentDto;
import static org.assertj.core.api.Assertions.assertThat;

class DocumentDtoTest {

    @Test
    @DisplayName("the six-args constructor stores each value and leaves storagePath null")
    void givenArgs_whenConstructed_shouldStoreFields() {
        var dto = createDocumentDto();

        assertThat(dto.getId()).isEqualTo(DOCUMENT_ID);
        assertThat(dto.getName()).isEqualTo("doc.pdf");
        assertThat(dto.getPath()).isEqualTo("balance/doc.pdf");
        assertThat(dto.getMimeType()).isEqualTo("application/pdf");
        assertThat(dto.getSize()).isEqualTo(10L);
        assertThat(dto.getHash()).isEqualTo("hash");
        assertThat(dto.getStoragePath()).isNull();
    }

    @Test
    @DisplayName("the no-args constructor and setters work")
    void givenNoArgs_whenSet_shouldExposeValues() {
        var dto = new DocumentDto();

        dto.setId(DOCUMENT_ID);
        dto.setName("n");
        dto.setPath("p");
        dto.setMimeType("m");
        dto.setSize(1L);
        dto.setHash("h");
        dto.setStoragePath("s");

        assertThat(dto.getId()).isEqualTo(DOCUMENT_ID);
        assertThat(dto.getName()).isEqualTo("n");
        assertThat(dto.getPath()).isEqualTo("p");
        assertThat(dto.getMimeType()).isEqualTo("m");
        assertThat(dto.getSize()).isEqualTo(1L);
        assertThat(dto.getHash()).isEqualTo("h");
        assertThat(dto.getStoragePath()).isEqualTo("s");
    }

    @Nested
    @DisplayName("getHyperlink")
    class GetHyperlink {

        @Test
        @DisplayName("should build a file URL from storage path, path, hash and the name extension")
        void givenDocument_shouldBuildFileUrl() {
            var dto = createDocumentDto();
            dto.setStoragePath("/storage");

            assertThat(dto.getHyperlink()).isEqualTo("file:///storage/balance/doc.pdf/hash.pdf");
        }
    }

    @Nested
    @DisplayName("isNotEmpty")
    class IsNotEmpty {

        @Test
        @DisplayName("should be true when the id is set")
        void givenId_shouldBeTrue() {
            assertThat(createDocumentDto().isNotEmpty()).isTrue();
        }

        @Test
        @DisplayName("should be false when the id is null")
        void givenNoId_shouldBeFalse() {
            assertThat(new DocumentDto().isNotEmpty()).isFalse();
        }
    }
}
