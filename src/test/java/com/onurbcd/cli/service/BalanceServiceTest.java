package com.onurbcd.cli.service;

import com.onurbcd.cli.config.property.AdminProperties;
import com.onurbcd.cli.dto.balance.BalanceDto;
import com.onurbcd.cli.dto.balance.BalanceSaveDto;
import com.onurbcd.cli.dto.balance.BalanceSumDto;
import com.onurbcd.cli.dto.document.DocumentDto;
import com.onurbcd.cli.dto.filter.BalanceFilter;
import com.onurbcd.cli.enums.BalanceType;
import com.onurbcd.cli.enums.Direction;
import com.onurbcd.cli.enums.ErrorCode;
import com.onurbcd.cli.exception.ApiException;
import com.onurbcd.cli.mapper.BalanceToEntityMapper;
import com.onurbcd.cli.param.CreateDocument;
import com.onurbcd.cli.param.MultipartFile;
import com.onurbcd.cli.param.SequenceParam;
import com.onurbcd.cli.persistency.entity.Balance;
import com.onurbcd.cli.persistency.entity.Day;
import com.onurbcd.cli.persistency.entity.Document;
import com.onurbcd.cli.persistency.predicate.BalancePredicateBuilder;
import com.onurbcd.cli.persistency.repository.BalanceRepository;
import com.onurbcd.cli.validator.BalanceValidator;
import com.querydsl.core.types.Predicate;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.onurbcd.cli.TestConstants.DOCUMENT_ID;
import static com.onurbcd.cli.TestConstants.ID;
import static com.onurbcd.cli.TestConstants.PARENT_ID;
import static com.onurbcd.cli.TestConstants.RECEIPT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BalanceServiceTest {

    private static final String STORAGE = "/storage";
    private static final LocalDate DATE = LocalDate.of(2024, 3, 5);
    private static final LocalDateTime CREATED = LocalDateTime.of(2024, 3, 15, 10, 30);
    private static final int NEW_DAY_ID = 20240305;
    private static final int CURRENT_DAY_ID = 20240301;
    private static final UUID SAVED_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @Mock
    private BalanceRepository repository;
    @Mock
    private BalanceToEntityMapper toEntityMapper;
    @Mock
    private BalanceValidator validator;
    @Mock
    private SequenceService sequenceService;
    @Mock
    private DayService dayService;
    @Mock
    private BalanceDocumentService documentService;
    @Mock
    private EntityManager entityManager;
    @Mock
    private BalanceSourceService sourceService;
    @Captor
    private ArgumentCaptor<SequenceParam> paramCaptor;
    @Captor
    private ArgumentCaptor<MultipartFile[]> filesCaptor;

    private BalanceService service;

    @BeforeEach
    void setUp() {
        var config = new AdminProperties();
        config.setStoragePath(STORAGE);
        service = new BalanceService(repository, toEntityMapper, validator, sequenceService, dayService,
                documentService, entityManager, sourceService, config);
    }

    private static void assertErrorCode(Throwable thrown, ErrorCode code) {
        assertThat(thrown).isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getError()).isEqualTo(code));
    }

    private static Day day(Integer id) {
        var day = new Day(id);
        day.setCalendarYear((short) 2024);
        day.setCalendarMonth((short) 3);
        return day;
    }

    private static BalanceSaveDto saveDto(MultipartFile... files) {
        return BalanceSaveDto.builder().dayCalendarDate(DATE).balanceType(BalanceType.INCOME)
                .multipartFiles(List.of(files)).build();
    }

    private static Balance mapped() {
        return Balance.builder().name("mapped").balanceType(BalanceType.OUTCOME).build();
    }

    private static Balance current(Short sequence, Day day) {
        return Balance.builder().id(ID).amount(new BigDecimal("10.00")).sequence(sequence).day(day)
                .balanceType(BalanceType.INCOME).createdDate(CREATED).build();
    }

    private static Document documentWithId(UUID id) {
        var document = new Document();
        document.setId(id);
        return document;
    }

    private static Balance persisted() {
        return Balance.builder().id(SAVED_ID).build();
    }

    private static void assertParam(SequenceParam param, Short sequence, Short target, BalanceType type) {
        assertThat(param.getYear()).isEqualTo((short) 2024);
        assertThat(param.getMonth()).isEqualTo((short) 3);
        assertThat(param.getSequence()).isEqualTo(sequence);
        assertThat(param.getTargetSequence()).isEqualTo(target);
        assertThat(param.getBalanceType()).isEqualTo(type);
    }

    @Nested
    @DisplayName("save(dto, id)")
    class SaveWithId {

        private final MultipartFile file = MultipartFile.builder().originalFilename("a.pdf").build();
        private final Document keep = documentWithId(DOCUMENT_ID);
        private final Document drop = documentWithId(RECEIPT_ID);

        @Test
        @DisplayName("for a new balance, should validate, create documents, persist, delete old files and update the source")
        void givenNewBalance_shouldPersistAndUpdateSource() {
            var dto = saveDto(file);
            var mapped = mapped();
            when(toEntityMapper.apply(dto)).thenReturn(mapped);
            when(documentService.createDocuments(eq(dto), any(MultipartFile[].class), eq(null)))
                    .thenReturn(CreateDocument.builder().saveDocuments(Set.of(keep)).deleteDocuments(Set.of(drop))
                            .build());
            when(sequenceService.getNextSequence(any(SequenceParam.class))).thenReturn((short) 6);
            when(dayService.createId(DATE)).thenReturn(NEW_DAY_ID);
            var dayProxy = day(NEW_DAY_ID);
            when(entityManager.getReference(Day.class, NEW_DAY_ID)).thenReturn(dayProxy);
            when(repository.saveAndFlush(mapped)).thenReturn(persisted());

            var result = service.save(dto, null);

            assertThat(result).isEqualTo(SAVED_ID.toString());
            assertThat(mapped.getId()).isNull();
            assertThat(mapped.getCreatedDate()).isNull();
            assertThat(mapped.getName()).isEqualTo("bogus");
            assertThat(mapped.getDocuments()).containsExactly(keep);
            assertThat(mapped.getSequence()).isEqualTo((short) 6);
            assertThat(mapped.getDay()).isSameAs(dayProxy);
            verify(validator).validate(dto, null, null);
            verify(documentService).createDocuments(eq(dto), filesCaptor.capture(), eq(null));
            assertThat(filesCaptor.getValue()).containsExactly(file);
            verify(sequenceService).getNextSequence(paramCaptor.capture());
            assertParam(paramCaptor.getValue(), null, null, BalanceType.OUTCOME);
            InOrder order = inOrder(repository, documentService, sourceService);
            order.verify(repository).saveAndFlush(mapped);
            order.verify(documentService).deleteDocuments(Set.of(drop));
            order.verify(sourceService).save(any(Balance.class), eq(null));
            verifyNoMoreInteractions(repository, documentService, sourceService, sequenceService, validator,
                    dayService, entityManager, toEntityMapper);
        }

        @Test
        @DisplayName("for an existing balance, should keep its identity, sequence and day, and pass the old amount")
        void givenExistingBalance_shouldKeepIdentitySequenceAndDay() {
            var dto = saveDto();
            var existing = current((short) 4, day(CURRENT_DAY_ID));
            var mapped = mapped();
            var dayProxy = day(CURRENT_DAY_ID);
            when(repository.get(ID)).thenReturn(Optional.of(existing));
            when(toEntityMapper.apply(dto)).thenReturn(mapped);
            when(documentService.createDocuments(eq(dto), any(MultipartFile[].class), eq(ID)))
                    .thenReturn(CreateDocument.builder().saveDocuments(null).deleteDocuments(Set.of()).build());
            when(entityManager.getReference(Day.class, CURRENT_DAY_ID)).thenReturn(dayProxy);
            var newBalance = persisted();
            when(repository.saveAndFlush(mapped)).thenReturn(newBalance);

            var result = service.save(dto, ID);

            assertThat(result).isEqualTo(SAVED_ID.toString());
            assertThat(mapped.getId()).isEqualTo(ID);
            assertThat(mapped.getCreatedDate()).isEqualTo(CREATED);
            assertThat(mapped.getSequence()).isEqualTo((short) 4);
            assertThat(mapped.getDay()).isSameAs(dayProxy);
            assertThat(mapped.getDocuments()).isNull();
            verify(repository).get(ID);
            verify(validator).validate(dto, existing, ID);
            verify(documentService).createDocuments(eq(dto), filesCaptor.capture(), eq(ID));
            assertThat(filesCaptor.getValue()).isEmpty();
            verify(documentService).deleteDocuments(Set.of());
            verify(repository).saveAndFlush(mapped);
            verify(sourceService).save(newBalance, new BigDecimal("10.00"));
            verifyNoMoreInteractions(repository, documentService, sourceService, validator, entityManager,
                    toEntityMapper);
            verifyNoInteractions(sequenceService, dayService);
        }

        @Test
        @DisplayName("for an existing balance without sequence nor day, should compute both")
        void givenExistingBalanceWithoutSequenceAndDay_shouldComputeBoth() {
            var dto = saveDto();
            var existing = current(null, null);
            var mapped = mapped();
            when(repository.get(ID)).thenReturn(Optional.of(existing));
            when(toEntityMapper.apply(dto)).thenReturn(mapped);
            when(documentService.createDocuments(eq(dto), any(MultipartFile[].class), eq(ID)))
                    .thenReturn(CreateDocument.builder().deleteDocuments(Set.of()).build());
            when(sequenceService.getNextSequence(any(SequenceParam.class))).thenReturn((short) 8);
            when(dayService.createId(DATE)).thenReturn(NEW_DAY_ID);
            when(entityManager.getReference(Day.class, NEW_DAY_ID)).thenReturn(day(NEW_DAY_ID));
            when(repository.saveAndFlush(mapped)).thenReturn(persisted());

            service.save(dto, ID);

            assertThat(mapped.getSequence()).isEqualTo((short) 8);
            assertThat(mapped.getDay().getId()).isEqualTo(NEW_DAY_ID);
            verify(sequenceService).getNextSequence(any(SequenceParam.class));
            verify(dayService).createId(DATE);
        }

        @Test
        @DisplayName("for an unknown id, should validate with a null current balance and pass a null old amount")
        void givenUnknownId_shouldTreatAsNoCurrentBalance() {
            var dto = saveDto();
            var mapped = mapped();
            when(repository.get(ID)).thenReturn(Optional.empty());
            when(toEntityMapper.apply(dto)).thenReturn(mapped);
            when(documentService.createDocuments(eq(dto), any(MultipartFile[].class), eq(null)))
                    .thenReturn(CreateDocument.builder().deleteDocuments(Set.of()).build());
            when(sequenceService.getNextSequence(any(SequenceParam.class))).thenReturn((short) 1);
            when(dayService.createId(DATE)).thenReturn(NEW_DAY_ID);
            when(entityManager.getReference(Day.class, NEW_DAY_ID)).thenReturn(day(NEW_DAY_ID));
            var newBalance = persisted();
            when(repository.saveAndFlush(mapped)).thenReturn(newBalance);

            service.save(dto, ID);

            verify(validator).validate(dto, null, ID);
            verify(sourceService).save(newBalance, null);
        }

        @Test
        @DisplayName("when validation fails, should propagate and neither store documents nor persist")
        void givenValidationFailure_shouldNotPersist() {
            var dto = saveDto();
            var failure = new ApiException(ErrorCode.DAY_IN_FUTURE);
            doThrow(failure).when(validator).validate(dto, null, null);

            assertThatThrownBy(() -> service.save(dto, null)).isSameAs(failure);

            verify(validator).validate(dto, null, null);
            verifyNoMoreInteractions(validator);
            verifyNoInteractions(repository, documentService, sourceService, toEntityMapper);
        }
    }

    @Nested
    @DisplayName("getSum")
    class GetSum {

        private BalanceFilter filter() {
            return BalanceFilter.of(true, "abc", null, null, null, null);
        }

        private Predicate predicateFor(BalanceType type) {
            var expected = filter();
            expected.setBalanceType(type);
            return BalancePredicateBuilder.all(expected);
        }

        @Test
        @DisplayName("should sum income and outcome separately and report their difference")
        void shouldReportIncomeOutcomeAndDiff() {
            var filter = filter();
            when(repository.getSum(predicateFor(BalanceType.INCOME))).thenReturn(new BigDecimal("100.00"));
            when(repository.getSum(predicateFor(BalanceType.OUTCOME))).thenReturn(new BigDecimal("30.00"));

            var result = service.getSum(filter);

            assertThat(result).usingRecursiveComparison().isEqualTo(List.of(
                    BalanceSumDto.income(new BigDecimal("100.00")), BalanceSumDto.outcome(new BigDecimal("30.00")),
                    BalanceSumDto.diff(new BigDecimal("70.00"))));
            InOrder order = inOrder(repository);
            order.verify(repository).getSum(predicateFor(BalanceType.INCOME));
            order.verify(repository).getSum(predicateFor(BalanceType.OUTCOME));
            verifyNoMoreInteractions(repository);
        }

        @Test
        @DisplayName("without any balance, should report zeros")
        void givenNoSums_shouldReportZeros() {
            when(repository.getSum(predicateFor(BalanceType.INCOME))).thenReturn(null);
            when(repository.getSum(predicateFor(BalanceType.OUTCOME))).thenReturn(null);

            var result = service.getSum(filter());

            assertThat(result).usingRecursiveComparison().isEqualTo(List.of(
                    BalanceSumDto.income(null), BalanceSumDto.outcome(null), BalanceSumDto.diff(BigDecimal.ZERO)));
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @EnumSource(BalanceType.class)
        @DisplayName("getSumByBalanceType should filter by the given type and leave it set on the filter")
        void givenType_shouldFilterByType(BalanceType type) {
            var filter = filter();
            when(repository.getSum(predicateFor(type))).thenReturn(new BigDecimal("5"));

            var result = service.getSumByBalanceType(filter, type);

            assertThat(result).isEqualTo(new BigDecimal("5"));
            assertThat(filter.getBalanceType()).isEqualTo(type);
            verify(repository).getSum(predicateFor(type));
            verifyNoMoreInteractions(repository);
        }
    }

    @Nested
    @DisplayName("save(dto)")
    class SaveBalanceDto {

        @Test
        @DisplayName("should create the balance with the next sequence of its day and type, then update the source")
        void givenDto_shouldPersistWithNextSequence() {
            var dto = saveDto();
            var mapped = mapped();
            var dayProxy = day(NEW_DAY_ID);
            var newBalance = persisted();
            when(toEntityMapper.apply(dto)).thenReturn(mapped);
            when(sequenceService.getNextSequence(any(SequenceParam.class))).thenReturn((short) 3);
            when(dayService.createId(DATE)).thenReturn(NEW_DAY_ID);
            when(entityManager.getReference(Day.class, NEW_DAY_ID)).thenReturn(dayProxy);
            when(repository.save(mapped)).thenReturn(newBalance);

            var result = service.save(dto);

            assertThat(result).isSameAs(newBalance);
            assertThat(mapped.getId()).isNull();
            assertThat(mapped.getSequence()).isEqualTo((short) 3);
            assertThat(mapped.getDay()).isSameAs(dayProxy);
            verify(sequenceService).getNextSequence(paramCaptor.capture());
            assertParam(paramCaptor.getValue(), null, null, BalanceType.INCOME);
            InOrder order = inOrder(repository, sourceService);
            order.verify(repository).save(mapped);
            order.verify(sourceService).save(newBalance, null);
            verify(toEntityMapper).apply(dto);
            verify(dayService).createId(DATE);
            verify(entityManager).getReference(Day.class, NEW_DAY_ID);
            verifyNoMoreInteractions(repository, sourceService, sequenceService, dayService, entityManager,
                    toEntityMapper);
            verifyNoInteractions(validator, documentService);
        }
    }

    @Nested
    @DisplayName("validate")
    class Validate {

        @Test
        @DisplayName("should delegate to the balance validator with the typed dto and entity")
        void shouldDelegateToValidator() {
            var dto = saveDto();
            var existing = current((short) 1, day(CURRENT_DAY_ID));

            service.validate(dto, existing, ID);

            verify(validator).validate(dto, existing, ID);
            verifyNoMoreInteractions(validator);
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAll {

        @Test
        @DisplayName("should query with the balance predicate through the custom query")
        void shouldUseBalancePredicate() {
            var filter = BalanceFilter.of(true, "abc", null, null, BalanceType.INCOME, null);
            var pageable = PageRequest.of(0, 10);
            var page = new PageImpl<>(List.of(new BalanceDto()), pageable, 1);
            when(repository.getAll(BalancePredicateBuilder.all(filter), pageable)).thenReturn(page);

            var result = service.getAll(pageable, filter);

            assertThat(result).isSameAs(page);
            verify(repository).getAll(BalancePredicateBuilder.all(filter), pageable);
            verifyNoMoreInteractions(repository);
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        private DocumentDto document(UUID id) {
            return new DocumentDto(id, "doc.pdf", "balance/2024", "application/pdf", 5L, "hash");
        }

        @Test
        @DisplayName("with documents, should expose each one with the storage path")
        void givenDocuments_shouldSetStoragePath() {
            var dto = new BalanceDto();
            var first = document(DOCUMENT_ID);
            var second = document(RECEIPT_ID);
            when(repository.getSingle(ID)).thenReturn(dto);
            when(repository.getDocumentsById(ID)).thenReturn(Set.of(first, second));

            var result = service.getById(ID);

            assertThat(result).isSameAs(dto);
            assertThat(result.getDocuments()).containsExactlyInAnyOrder(first, second);
            assertThat(result.getDocuments()).allSatisfy(d -> assertThat(d.getStoragePath()).isEqualTo(STORAGE));
            verify(repository).getSingle(ID);
            verify(repository).getDocumentsById(ID);
            verifyNoMoreInteractions(repository);
        }

        @Test
        @DisplayName("without documents, should set them to null")
        void givenNoDocuments_shouldSetNull() {
            var dto = new BalanceDto();
            dto.setDocuments(Set.of(document(DOCUMENT_ID)));
            when(repository.getSingle(ID)).thenReturn(dto);
            when(repository.getDocumentsById(ID)).thenReturn(Set.of());

            var result = service.getById(ID);

            assertThat(result.getDocuments()).isNull();
        }

        @Test
        @DisplayName("when the balance does not exist, should fail with not found without loading documents")
        void givenUnknownBalance_shouldThrowNotFound() {
            when(repository.getSingle(ID)).thenReturn(null);

            assertThatThrownBy(() -> service.getById(ID))
                    .satisfies(e -> assertErrorCode(e, ErrorCode.ENTITY_DOES_NOT_EXIST));

            verify(repository).getSingle(ID);
            verifyNoMoreInteractions(repository);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should delete the row, close the sequence gap, then delete the files and revert the source")
        void givenBalance_shouldDeleteAndRevert() {
            var balance = current((short) 3, day(CURRENT_DAY_ID));
            var documents = Set.of(documentWithId(DOCUMENT_ID));
            when(repository.get(ID)).thenReturn(Optional.of(balance));
            when(repository.getDocuments(ID)).thenReturn(documents);

            service.delete(ID);

            InOrder order = inOrder(repository, sequenceService, documentService, sourceService);
            order.verify(repository).get(ID);
            order.verify(repository).getDocuments(ID);
            order.verify(repository).deleteById(ID);
            order.verify(sequenceService).updateNextSequences(paramCaptor.capture());
            order.verify(documentService).deleteDocuments(documents);
            order.verify(sourceService).delete(balance);
            assertParam(paramCaptor.getValue(), (short) 3, null, BalanceType.INCOME);
            verifyNoMoreInteractions(repository, sequenceService, documentService, sourceService);
        }

        @Test
        @DisplayName("when the balance does not exist, should fail with not found and delete nothing")
        void givenUnknownBalance_shouldThrowNotFound() {
            when(repository.get(ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.delete(ID))
                    .satisfies(e -> assertErrorCode(e, ErrorCode.ENTITY_DOES_NOT_EXIST));

            verify(repository).get(ID);
            verifyNoMoreInteractions(repository);
            verifyNoInteractions(sequenceService, documentService, sourceService);
        }
    }

    @Nested
    @DisplayName("updateSequence")
    class UpdateSequence {

        @ParameterizedTest(name = "[{index}] {0}")
        @EnumSource(Direction.class)
        @DisplayName("should swap the sequence of the balance's day and type in the given direction")
        void givenBalance_shouldSwapSequence(Direction direction) {
            when(repository.get(ID)).thenReturn(Optional.of(current((short) 3, day(CURRENT_DAY_ID))));

            service.updateSequence(ID, direction);

            verify(repository).get(ID);
            verify(sequenceService).swapSequence(paramCaptor.capture(), eq(direction));
            assertParam(paramCaptor.getValue(), (short) 3, null, BalanceType.INCOME);
            verifyNoMoreInteractions(repository, sequenceService);
        }

        @Test
        @DisplayName("when the balance does not exist, should fail with not found and not swap")
        void givenUnknownBalance_shouldThrowNotFound() {
            when(repository.get(ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateSequence(ID, Direction.UP))
                    .satisfies(e -> assertErrorCode(e, ErrorCode.ENTITY_DOES_NOT_EXIST));

            verify(repository).get(ID);
            verifyNoMoreInteractions(repository);
            verifyNoInteractions(sequenceService);
        }
    }

    @Nested
    @DisplayName("swapPosition")
    class SwapPosition {

        @Test
        @DisplayName("should swap the balance to the target sequence within its day and type")
        void givenBalance_shouldSwapPosition() {
            when(repository.get(ID)).thenReturn(Optional.of(current((short) 3, day(CURRENT_DAY_ID))));

            service.swapPosition(ID, (short) 7);

            verify(repository).get(ID);
            verify(sequenceService).swapPosition(paramCaptor.capture());
            assertParam(paramCaptor.getValue(), (short) 3, (short) 7, BalanceType.INCOME);
            verifyNoMoreInteractions(repository, sequenceService);
        }

        @Test
        @DisplayName("when the balance does not exist, should fail with not found and not swap")
        void givenUnknownBalance_shouldThrowNotFound() {
            when(repository.get(PARENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.swapPosition(PARENT_ID, (short) 7))
                    .satisfies(e -> assertErrorCode(e, ErrorCode.ENTITY_DOES_NOT_EXIST));

            verify(repository).get(PARENT_ID);
            verifyNoMoreInteractions(repository);
            verifyNoInteractions(sequenceService);
        }
    }
}
