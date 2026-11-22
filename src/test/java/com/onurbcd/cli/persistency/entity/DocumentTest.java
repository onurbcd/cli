package com.onurbcd.cli.persistency.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.onurbcd.cli.TestConstants.DOCUMENT_ID;
import static org.assertj.core.api.Assertions.assertThat;

class DocumentTest {

    private static final UUID OTHER_ID = UUID.fromString("99999999-9999-9999-0000-000000000001");

    private static Document document(UUID id) {
        var document = new Document();
        document.setId(id);
        return document;
    }

    @Nested
    @DisplayName("equals")
    class Equals {

        @Test
        @DisplayName("should be true for the same instance")
        void givenSameInstance_whenEquals_shouldBeTrue() {
            var document = document(DOCUMENT_ID);

            assertThat(document.equals(document)).isTrue();
        }

        @Test
        @DisplayName("should be true, in both directions, for the same id")
        void givenSameId_whenEquals_shouldBeTrueBothWays() {
            var first = document(DOCUMENT_ID);
            var second = document(DOCUMENT_ID);

            assertThat(first.equals(second)).isTrue();
            assertThat(second.equals(first)).isTrue();
        }

        @Test
        @DisplayName("should be false for a different id")
        void givenDifferentId_whenEquals_shouldBeFalse() {
            assertThat(document(DOCUMENT_ID).equals(document(OTHER_ID))).isFalse();
        }

        @Test
        @DisplayName("should be false for null")
        void givenNull_whenEquals_shouldBeFalse() {
            assertThat(document(DOCUMENT_ID).equals(null)).isFalse();
        }

        @Test
        @DisplayName("should be false for a subclass with the same id, in both directions")
        void givenSubclassWithSameId_whenEquals_shouldBeFalse() {
            var document = document(DOCUMENT_ID);
            var subclass = new Document() {
            };
            subclass.setId(DOCUMENT_ID);

            assertThat(document.equals(subclass)).isFalse();
            assertThat(subclass.equals(document)).isFalse();
        }

        @Test
        @DisplayName("should be false for another type")
        void givenOtherType_whenEquals_shouldBeFalse() {
            Object notADocument = DOCUMENT_ID;

            assertThat(document(DOCUMENT_ID).equals(notADocument)).isFalse();
        }
    }

    @Test
    @DisplayName("a document is always new, so Spring Data never selects before inserting")
    void givenDocument_whenIsNew_shouldBeTrue() {
        assertThat(document(DOCUMENT_ID).isNew()).isTrue();
        assertThat(new Document().isNew()).isTrue();
    }

    @Test
    @DisplayName("setters store each field")
    void givenValues_whenSet_shouldReturnThemFromGetters() {
        var document = new Document();

        document.setId(DOCUMENT_ID);
        document.setName("invoice.pdf");
        document.setPath("docs/invoice.pdf");
        document.setMimeType("application/pdf");
        document.setSize(2048L);
        document.setHash("d41d8cd98f00b204e9800998ecf8427e");

        assertThat(document.getId()).isEqualTo(DOCUMENT_ID);
        assertThat(document.getName()).isEqualTo("invoice.pdf");
        assertThat(document.getPath()).isEqualTo("docs/invoice.pdf");
        assertThat(document.getMimeType()).isEqualTo("application/pdf");
        assertThat(document.getSize()).isEqualTo(2048L);
        assertThat(document.getHash()).isEqualTo("d41d8cd98f00b204e9800998ecf8427e");
    }

    @Nested
    @DisplayName("hashCode")
    class HashCode {

        @Test
        @DisplayName("should be the same for equal documents and differ for different ids")
        void givenIds_whenHashCode_shouldFollowEquals() {
            assertThat(document(DOCUMENT_ID)).hasSameHashCodeAs(document(DOCUMENT_ID));
            assertThat(document(DOCUMENT_ID)).doesNotHaveSameHashCodeAs(document(OTHER_ID));
        }
    }
}
