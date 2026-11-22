package com.onurbcd.cli.service;

import com.onurbcd.cli.dto.balance.BalanceSaveDto;
import com.onurbcd.cli.dto.document.MultipartFileDto;
import com.onurbcd.cli.param.MultipartFile;
import com.onurbcd.cli.persistency.entity.Document;
import com.onurbcd.cli.persistency.repository.BalanceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.onurbcd.cli.TestConstants.DOCUMENT_ID;
import static com.onurbcd.cli.TestConstants.ID;
import static com.onurbcd.cli.TestConstants.RECEIPT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BalanceDocumentServiceTest {

    private static final UUID OTHER_ID = UUID.fromString("88888888-8888-8888-8888-888888888888");

    @Mock
    private DocumentService documentService;
    @Mock
    private BalanceRepository balanceRepository;
    @Captor
    private ArgumentCaptor<MultipartFileDto> dtoCaptor;
    @InjectMocks
    private BalanceDocumentService service;

    private static Document document(UUID id) {
        var document = new Document();
        document.setId(id);
        document.setName(id + ".pdf");
        return document;
    }

    private static BalanceSaveDto saveDto(Set<UUID> documentsIds) {
        return BalanceSaveDto.builder().dayCalendarDate(LocalDate.of(2024, 3, 5)).documentsIds(documentsIds).build();
    }

    @Nested
    @DisplayName("createDocuments")
    class CreateDocuments {

        private final MultipartFile file = MultipartFile.builder().originalFilename("a.pdf").build();

        @ParameterizedTest(name = "[{index}] files={0}")
        @NullAndEmptySource
        @DisplayName("for a new balance without files, should save and delete nothing")
        void givenNewBalanceWithoutFiles_shouldReturnNothing(MultipartFile[] files) {
            var result = service.createDocuments(saveDto(null), files, null);

            assertThat(result.getSaveDocuments()).isNull();
            assertThat(result.getDeleteDocuments()).isEmpty();
            verifyNoInteractions(documentService, balanceRepository);
        }

        @Test
        @DisplayName("for a new balance with files, should save them under the day path and return them")
        void givenNewBalanceWithFiles_shouldSaveUnderDayPath() {
            var saved = Set.of(document(DOCUMENT_ID));
            var files = new MultipartFile[]{file};
            when(documentService.save(any(MultipartFileDto.class))).thenReturn(saved);

            var result = service.createDocuments(saveDto(null), files, null);

            assertThat(result.getSaveDocuments()).isEqualTo(saved);
            assertThat(result.getDeleteDocuments()).isEmpty();
            verify(documentService).save(dtoCaptor.capture());
            assertThat(dtoCaptor.getValue().getPath()).isEqualTo("balance/2024/03/05");
            assertThat(dtoCaptor.getValue().getMultipartFiles()).containsExactly(file);
            assertThat(dtoCaptor.getValue().getMultipartFile()).isNull();
            verifyNoMoreInteractions(documentService);
            verifyNoInteractions(balanceRepository);
        }

        @Test
        @DisplayName("for an existing balance, should keep the documents still listed, delete the rest and add new files")
        void givenExistingBalance_shouldKeepDeleteAndAdd() {
            var kept = document(DOCUMENT_ID);
            var removed = document(RECEIPT_ID);
            var added = document(OTHER_ID);
            var files = new MultipartFile[]{file};
            when(balanceRepository.getDocuments(ID)).thenReturn(new HashSet<>(Set.of(kept, removed)));
            when(documentService.save(any(MultipartFileDto.class))).thenReturn(Set.of(added));

            var result = service.createDocuments(saveDto(Set.of(DOCUMENT_ID)), files, ID);

            assertThat(result.getSaveDocuments()).containsExactlyInAnyOrder(kept, added);
            assertThat(result.getDeleteDocuments()).containsExactly(removed);
            verify(balanceRepository).getDocuments(ID);
            verify(documentService).save(any(MultipartFileDto.class));
            verifyNoMoreInteractions(documentService, balanceRepository);
        }

        @Test
        @DisplayName("for an existing balance without documents ids, should delete every current document")
        void givenExistingBalanceWithoutIds_shouldDeleteAll() {
            var current = document(DOCUMENT_ID);
            when(balanceRepository.getDocuments(ID)).thenReturn(Set.of(current));

            var result = service.createDocuments(saveDto(null), null, ID);

            assertThat(result.getSaveDocuments()).isNull();
            assertThat(result.getDeleteDocuments()).containsExactly(current);
            verify(balanceRepository).getDocuments(ID);
            verifyNoMoreInteractions(balanceRepository);
            verifyNoInteractions(documentService);
        }

        @Test
        @DisplayName("for an existing balance with all current documents still listed, should delete nothing")
        void givenAllIdsStillListed_shouldKeepAll() {
            var current = document(DOCUMENT_ID);
            when(balanceRepository.getDocuments(ID)).thenReturn(Set.of(current));

            var result = service.createDocuments(saveDto(Set.of(DOCUMENT_ID)), null, ID);

            assertThat(result.getSaveDocuments()).containsExactly(current);
            assertThat(result.getDeleteDocuments()).isEmpty();
            verify(balanceRepository).getDocuments(ID);
            verifyNoMoreInteractions(balanceRepository);
            verifyNoInteractions(documentService);
        }

        @Test
        @DisplayName("for an existing balance without documents, should save and delete nothing")
        void givenExistingBalanceWithoutDocuments_shouldReturnNothing() {
            when(balanceRepository.getDocuments(ID)).thenReturn(Set.of());

            var result = service.createDocuments(saveDto(Set.of(DOCUMENT_ID)), null, ID);

            assertThat(result.getSaveDocuments()).isNull();
            assertThat(result.getDeleteDocuments()).isEmpty();
            verify(balanceRepository).getDocuments(ID);
            verifyNoMoreInteractions(balanceRepository);
            verifyNoInteractions(documentService);
        }

        @Test
        @DisplayName("when the repository returns null documents, should treat them as none")
        void givenNullDocuments_shouldReturnNothing() {
            when(balanceRepository.getDocuments(ID)).thenReturn(null);

            var result = service.createDocuments(saveDto(null), null, ID);

            assertThat(result.getSaveDocuments()).isNull();
            assertThat(result.getDeleteDocuments()).isEmpty();
            verify(balanceRepository).getDocuments(ID);
            verifyNoMoreInteractions(balanceRepository);
            verifyNoInteractions(documentService);
        }
    }

    @Nested
    @DisplayName("deleteDocuments")
    class DeleteDocuments {

        @ParameterizedTest(name = "[{index}] documents={0}")
        @NullAndEmptySource
        @DisplayName("with no documents, should not call the document service")
        void givenNoDocuments_shouldDoNothing(Set<Document> documents) {
            service.deleteDocuments(documents);

            verifyNoInteractions(documentService, balanceRepository);
        }

        @Test
        @DisplayName("with documents, should delegate the deletion to the document service")
        void givenDocuments_shouldDelegate() {
            var documents = Set.of(document(DOCUMENT_ID));

            service.deleteDocuments(documents);

            verify(documentService).delete(documents);
            verifyNoMoreInteractions(documentService);
            verifyNoInteractions(balanceRepository);
        }
    }
}
