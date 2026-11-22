package com.onurbcd.cli.param;

import com.onurbcd.cli.persistency.entity.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static com.onurbcd.cli.TestConstants.DOCUMENT_ID;
import static org.assertj.core.api.Assertions.assertThat;

class CreateDocumentTest {

    private static Document document() {
        var document = new Document();
        document.setId(DOCUMENT_ID);
        document.setName("doc.txt");
        return document;
    }

    static Stream<Set<Document>> emptySaveDocuments() {
        var withNullElement = new HashSet<Document>();
        withNullElement.add(null);

        return Stream.of(Set.of(), withNullElement, Set.of(new Document()));
    }

    @Nested
    @DisplayName("getSaveDocuments")
    class GetSaveDocuments {

        @ParameterizedTest(name = "[{index}] {0}")
        @NullSource
        @MethodSource("com.onurbcd.cli.param.CreateDocumentTest#emptySaveDocuments")
        @DisplayName("when documents are null, empty or hold only blank documents, should return null")
        void givenNoRealDocuments_whenGetSaveDocuments_shouldReturnNull(Set<Document> saveDocuments) {
            var param = CreateDocument.builder().saveDocuments(saveDocuments).build();

            assertThat(param.getSaveDocuments()).isNull();
        }

        @Test
        @DisplayName("when a document has data, should return the same documents")
        void givenRealDocument_whenGetSaveDocuments_shouldReturnThem() {
            var documents = Set.of(document());
            var param = CreateDocument.builder().saveDocuments(documents).build();

            assertThat(param.getSaveDocuments()).isSameAs(documents);
        }
    }

    @Nested
    @DisplayName("getDeleteDocuments")
    class GetDeleteDocuments {

        @Test
        @DisplayName("should return the delete documents untouched, even when they are blank")
        void givenBlankDeleteDocuments_whenGetDeleteDocuments_shouldReturnThem() {
            var documents = Set.of(new Document());
            var param = CreateDocument.builder().deleteDocuments(documents).build();

            assertThat(param.getDeleteDocuments()).isSameAs(documents);
        }
    }
}
