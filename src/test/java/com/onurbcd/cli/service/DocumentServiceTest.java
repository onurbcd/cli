package com.onurbcd.cli.service;

import com.onurbcd.cli.dto.document.DocumentDto;
import com.onurbcd.cli.dto.document.MultipartFileDto;
import com.onurbcd.cli.enums.ErrorCode;
import com.onurbcd.cli.exception.ApiException;
import com.onurbcd.cli.mapper.DocumentToDtoMapper;
import com.onurbcd.cli.param.MultipartFile;
import com.onurbcd.cli.persistency.entity.Document;
import com.onurbcd.cli.persistency.repository.DocumentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.onurbcd.cli.TestConstants.DOCUMENT_ID;
import static com.onurbcd.cli.TestConstants.ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    private static final String DOCUMENT_IDENTIFIER_PATTERN = "[0-9a-f]{32}";
    private static final String PATH = "balance/2024/03/05";
    private static final String MIME = "application/pdf";

    @Mock
    private DocumentRepository repository;
    @Mock
    private StorageService storageService;
    @Mock
    private DocumentToDtoMapper toDtoMapper;
    @Captor
    private ArgumentCaptor<Document> documentCaptor;
    @Captor
    private ArgumentCaptor<List<Document>> listCaptor;
    @InjectMocks
    private DocumentService service;

    private static MultipartFile upload(String name, String contentType, Long size) {
        return MultipartFile.builder().originalFilename(name).contentType(contentType).size(size).build();
    }

    private static MultipartFileDto many(MultipartFile... files) {
        return MultipartFileDto.builder().path(PATH).multipartFiles(files).build();
    }

    private static MultipartFileDto one(MultipartFile file, String name) {
        return MultipartFileDto.builder().path(PATH).name(name).multipartFile(file).build();
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("should validate, create and store each file, then persist all documents at once")
        void givenFiles_shouldStoreAndPersistAll() {
            var first = upload("a.pdf", MIME, 10L);
            var second = upload("b.pdf", MIME, 20L);
            var persisted = new Document();
            persisted.setId(DOCUMENT_ID);
            when(repository.saveAll(any())).thenReturn(List.of(persisted));
            var result = service.save(many(first, second));

            assertThat(result).containsExactly(persisted);
            InOrder order = inOrder(storageService, repository);
            order.verify(storageService).saveFile(documentCaptor.capture(), same(first));
            order.verify(storageService).saveFile(documentCaptor.capture(), same(second));
            order.verify(repository).saveAll(listCaptor.capture());
            var documents = documentCaptor.getAllValues();
            assertThat(listCaptor.getValue()).containsExactlyElementsOf(documents);
            assertThat(documents).extracting(Document::getName).containsExactly("a.pdf", "b.pdf");
            assertThat(documents).extracting(Document::getPath).containsOnly(PATH);
            assertThat(documents).extracting(Document::getMimeType).containsOnly(MIME);
            assertThat(documents).extracting(Document::getSize).containsExactly(10L, 20L);
            var identifiers = documents.stream().map(Document::getHash).toList();
            assertThat(identifiers)
                    .allSatisfy(identifier -> assertThat(identifier).matches(DOCUMENT_IDENTIFIER_PATTERN))
                    .doesNotHaveDuplicates();
            verifyNoMoreInteractions(storageService, repository);
            verifyNoInteractions(toDtoMapper);
        }

        @Test
        @DisplayName("without files, should persist an empty list and store nothing")
        void givenNoFiles_shouldPersistNothing() {
            when(repository.saveAll(any())).thenReturn(List.of());

            var result = service.save(many());

            assertThat(result).isEmpty();
            verify(repository).saveAll(listCaptor.capture());
            assertThat(listCaptor.getValue()).isEmpty();
            verifyNoMoreInteractions(repository);
            verifyNoInteractions(storageService);
        }

        @Test
        @DisplayName("when a file is invalid, should stop before storing it and persisting anything")
        void givenInvalidSecondFile_shouldStopBeforePersisting() {
            var valid = upload("a.pdf", MIME, 10L);
            var invalid = upload(" ", MIME, 10L);
            var dto = many(valid, invalid);

            assertThatThrownBy(() -> service.save(dto))
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getError()).isEqualTo(ErrorCode.DOCUMENT_NAME_IS_BLANK));

            verify(storageService).saveFile(any(Document.class), same(valid));
            verifyNoMoreInteractions(storageService);
            verifyNoInteractions(repository);
        }
    }

    @Nested
    @DisplayName("saveOne")
    class SaveOne {

        @Test
        @DisplayName("should store the file with the given name and persist a single document")
        void givenFile_shouldUseGivenName() {
            var file = upload("original.pdf", MIME, 10L);
            var persisted = new Document();
            when(repository.save(any(Document.class))).thenReturn(persisted);
            var result = service.saveOne(one(file, "custom.pdf"));

            assertThat(result).isSameAs(persisted);
            verify(storageService).saveFile(documentCaptor.capture(), same(file));
            verify(repository).save(documentCaptor.getValue());
            var document = documentCaptor.getValue();
            assertThat(document.getName()).isEqualTo("custom.pdf");
            assertThat(document.getPath()).isEqualTo(PATH);
            assertThat(document.getMimeType()).isEqualTo(MIME);
            assertThat(document.getSize()).isEqualTo(10L);
            assertThat(document.getHash()).matches(DOCUMENT_IDENTIFIER_PATTERN);
            verifyNoMoreInteractions(storageService, repository);
        }

        @Test
        @DisplayName("when the file is invalid, should not store or persist")
        void givenInvalidFile_shouldNotStore() {
            var dto = one(upload("a.pdf", MIME, 0L), "custom.pdf");

            assertThatThrownBy(() -> service.saveOne(dto))
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getError()).isEqualTo(ErrorCode.DOCUMENT_IS_EMPTY));

            verifyNoInteractions(storageService, repository);
        }
    }

    @Nested
    @DisplayName("validation")
    class Validation {

        @Test
        @DisplayName("with an empty file (null size), should fail with document is empty")
        void givenNullSize_shouldFailEmpty() {
            var dto = many(upload("a.pdf", MIME, null));

            assertThatThrownBy(() -> service.save(dto))
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getError()).isEqualTo(ErrorCode.DOCUMENT_IS_EMPTY));

            verifyNoInteractions(storageService, repository);
        }

        @Test
        @DisplayName("with an empty file (zero size), should fail with document is empty")
        void givenZeroSize_shouldFailEmpty() {
            var dto = many(upload("a.pdf", MIME, 0L));

            assertThatThrownBy(() -> service.save(dto))
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getError()).isEqualTo(ErrorCode.DOCUMENT_IS_EMPTY));

            verifyNoInteractions(storageService, repository);
        }

        @ParameterizedTest(name = "[{index}] name=''{0}''")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t"})
        @DisplayName("with a blank name, should fail with document name is blank")
        void givenBlankName_shouldFail(String name) {
            var dto = many(upload(name, MIME, 10L));

            assertThatThrownBy(() -> service.save(dto))
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getError()).isEqualTo(ErrorCode.DOCUMENT_NAME_IS_BLANK));

            verifyNoInteractions(storageService, repository);
        }

        @ParameterizedTest(name = "[{index}] contentType=''{0}''")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t"})
        @DisplayName("with a blank content type, should fail with document mime type is blank")
        void givenBlankContentType_shouldFail(String contentType) {
            var dto = many(upload("a.pdf", contentType, 10L));

            assertThatThrownBy(() -> service.save(dto))
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getError()).isEqualTo(ErrorCode.DOCUMENT_MIME_TYPE_IS_BLANK));

            verifyNoInteractions(storageService, repository);
        }

        @Test
        @DisplayName("when a file reports not empty but has zero size, should fail with document size is zero")
        void givenInconsistentFile_shouldFailWithSizeZero() {
            var inconsistent = new MultipartFile("a.pdf", MIME, 0L, null) {
                @Override
                public boolean isEmpty() {
                    return false;
                }
            };
            var dto = many(inconsistent);

            assertThatThrownBy(() -> service.save(dto))
                    .isInstanceOfSatisfying(ApiException.class,
                            e -> assertThat(e.getError()).isEqualTo(ErrorCode.DOCUMENT_SIZE_IS_ZERO));

            verifyNoInteractions(storageService, repository);
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAll {

        @Test
        @DisplayName("should map every stored document to a dto keeping the page metadata")
        void shouldMapPage() {
            var pageable = PageRequest.of(1, 2);
            var document = new Document();
            var dto = new DocumentDto(DOCUMENT_ID, "a.pdf", PATH, MIME, 10L, "hash");
            when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(document), pageable, 3));
            when(toDtoMapper.apply(document)).thenReturn(dto);

            var result = service.getAll(pageable);

            assertThat(result.getContent()).containsExactly(dto);
            assertThat(result.getTotalElements()).isEqualTo(3);
            verify(repository).findAll(pageable);
            verify(toDtoMapper).apply(document);
            verifyNoMoreInteractions(repository, toDtoMapper);
            verifyNoInteractions(storageService);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        private static Document withId(UUID id) {
            var document = new Document();
            document.setId(id);
            return document;
        }

        @Test
        @DisplayName("should delete the stored file before the database row, for each document")
        void givenDocuments_shouldDeleteFileThenRow() {
            var first = withId(ID);
            var second = withId(DOCUMENT_ID);

            service.delete(new LinkedHashSet<>(List.of(first, second)));

            var order = inOrder(storageService, repository);
            order.verify(storageService).deleteFile(first);
            order.verify(repository).deleteUsingId(ID);
            order.verify(storageService).deleteFile(second);
            order.verify(repository).deleteUsingId(DOCUMENT_ID);
            verifyNoMoreInteractions(storageService, repository);
        }

        @Test
        @DisplayName("without documents, should do nothing")
        void givenNoDocuments_shouldDoNothing() {
            service.delete(Set.of());

            verifyNoInteractions(storageService, repository);
        }

        @Test
        @DisplayName("when the file deletion fails, should propagate and keep the database row")
        void givenStorageFailure_shouldKeepRow() {
            var document = withId(ID);
            var failure = new ApiException(ErrorCode.STORAGE_FILE_DELETE, new RuntimeException("io"));
            doThrow(failure).when(storageService).deleteFile(document);

            assertThatThrownBy(() -> service.delete(Set.of(document))).isSameAs(failure);

            verify(storageService).deleteFile(document);
            verifyNoMoreInteractions(storageService);
            verifyNoInteractions(repository);
        }
    }
}
