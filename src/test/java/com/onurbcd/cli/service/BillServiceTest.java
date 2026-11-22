package com.onurbcd.cli.service;

import com.onurbcd.cli.config.property.AdminProperties;
import com.onurbcd.cli.dto.ItemDto;
import com.onurbcd.cli.dto.bill.BillCloseDto;
import com.onurbcd.cli.dto.bill.BillDto;
import com.onurbcd.cli.dto.bill.BillOpenDto;
import com.onurbcd.cli.dto.bill.BillSaveDto;
import com.onurbcd.cli.dto.billtype.BillTypeValuesDto;
import com.onurbcd.cli.dto.budget.BudgetPatchDto;
import com.onurbcd.cli.dto.budget.BudgetValuesDto;
import com.onurbcd.cli.dto.document.DocumentDto;
import com.onurbcd.cli.dto.filter.BillFilter;
import com.onurbcd.cli.dto.incomesource.IncomeSourceSaveDto;
import com.onurbcd.cli.enums.DocumentType;
import com.onurbcd.cli.enums.ErrorCode;
import com.onurbcd.cli.enums.PaymentType;
import com.onurbcd.cli.enums.ReferenceType;
import com.onurbcd.cli.exception.ApiException;
import com.onurbcd.cli.mapper.BillOpenToEntityMapper;
import com.onurbcd.cli.param.BillBalanceParams;
import com.onurbcd.cli.param.BillDocParams;
import com.onurbcd.cli.param.MultipartFile;
import com.onurbcd.cli.persistency.entity.Balance;
import com.onurbcd.cli.persistency.entity.Bill;
import com.onurbcd.cli.persistency.entity.BillType;
import com.onurbcd.cli.persistency.entity.Budget;
import com.onurbcd.cli.persistency.entity.Day;
import com.onurbcd.cli.persistency.entity.Document;
import com.onurbcd.cli.persistency.predicate.BillPredicateBuilder;
import com.onurbcd.cli.persistency.repository.BillRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.shell.jline.tui.component.flow.SelectItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.onurbcd.cli.TestConstants.BILL_TYPE_ID;
import static com.onurbcd.cli.TestConstants.CATEGORY_ID;
import static com.onurbcd.cli.TestConstants.DOCUMENT_ID;
import static com.onurbcd.cli.TestConstants.ID;
import static com.onurbcd.cli.TestConstants.PARENT_ID;
import static com.onurbcd.cli.TestConstants.RECEIPT_ID;
import static com.onurbcd.cli.TestConstants.SOURCE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillServiceTest {

    private static final String STORAGE = "/storage";
    private static final LocalDate REFERENCE = LocalDate.of(2024, 3, 1);
    private static final LocalDate DOCUMENT_DATE = LocalDate.of(2024, 3, 5);
    private static final LocalDate DUE = LocalDate.of(2024, 3, 20);
    private static final LocalDate PAYMENT = LocalDate.of(2024, 3, 18);
    private static final BigDecimal AMOUNT = new BigDecimal("55.90");
    private static final UUID SAVED_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @Mock
    private BillRepository repository;
    @Mock
    private BillOpenToEntityMapper toEntityMapper;
    @Mock
    private DayService dayService;
    @Mock
    private EntityManager entityManager;
    @Mock
    private BillDocumentService documentService;
    @Mock
    private BudgetService budgetService;
    @Mock
    private BillTypeService billTypeService;
    @Mock
    private BillBalanceService billBalanceService;
    @Captor
    private ArgumentCaptor<BillDocParams> docParamsCaptor;
    @Captor
    private ArgumentCaptor<BillBalanceParams> balanceParamsCaptor;
    @Captor
    private ArgumentCaptor<BudgetPatchDto> patchCaptor;

    private BillService service;

    @BeforeEach
    void setUp() {
        var config = new AdminProperties();
        config.setStoragePath(STORAGE);
        service = new BillService(repository, toEntityMapper, dayService, entityManager, documentService,
                budgetService, billTypeService, billBalanceService, config);
    }

    private static void assertErrorCode(Throwable thrown, ErrorCode code) {
        assertThat(thrown).isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getError()).isEqualTo(code));
    }

    private static MultipartFile upload() {
        return MultipartFile.builder().originalFilename("bill.pdf").contentType("application/pdf").size(5L).build();
    }

    private static BillOpenDto openDto(LocalDate documentDate) {
        return BillOpenDto.builder().name("name").active(true).referenceDayCalendarDate(REFERENCE)
                .documentDateCalendarDate(documentDate).dueDateCalendarDate(DUE).budgetId(PARENT_ID)
                .documentType(DocumentType.BILLET).referenceType(ReferenceType.MONTH).multipartFile(upload()).build();
    }

    private static BudgetValuesDto budgetValues(Boolean paid) {
        return new BudgetValuesDto(AMOUNT, BILL_TYPE_ID, "bills/water", paid);
    }

    private static Bill saved(Budget budget) {
        return Bill.builder().id(SAVED_ID).budget(budget).build();
    }

    private void stubDay(LocalDate date, int dayId) {
        when(dayService.createId(date)).thenReturn(dayId);
        when(entityManager.getReference(Day.class, dayId)).thenReturn(new Day(dayId));
    }

    @Nested
    @DisplayName("save an open bill")
    class SaveOpenBill {

        @Test
        @DisplayName("should validate, store the document and persist the open bill with its days, value and bill type")
        void givenOpenDto_shouldPersistOpenBill() {
            var dto = openDto(DOCUMENT_DATE);
            var mapped = Bill.builder().name("mapped").build();
            var document = new Document();
            var billType = BillType.builder().id(BILL_TYPE_ID).build();
            when(toEntityMapper.apply(dto)).thenReturn(mapped);
            when(repository.countByBudgetId(PARENT_ID)).thenReturn(0L);
            when(budgetService.getBudgetValues(PARENT_ID)).thenReturn(budgetValues(false));
            when(documentService.createDocument(any(BillDocParams.class))).thenReturn(document);
            stubDay(REFERENCE, 20240301);
            stubDay(DOCUMENT_DATE, 20240305);
            stubDay(DUE, 20240320);
            when(entityManager.getReference(BillType.class, BILL_TYPE_ID)).thenReturn(billType);
            when(repository.save(mapped)).thenReturn(saved(null));

            var result = service.save(dto, null);

            assertThat(result).isEqualTo(SAVED_ID.toString());
            assertThat(mapped.getName()).isEqualTo("bogus");
            assertThat(mapped.getReferenceDay()).isEqualTo(new Day(20240301));
            assertThat(mapped.getDocumentDate()).isEqualTo(new Day(20240305));
            assertThat(mapped.getDueDate()).isEqualTo(new Day(20240320));
            assertThat(mapped.getValue()).isEqualTo(AMOUNT);
            assertThat(mapped.getBillDocument()).isSameAs(document);
            assertThat(mapped.getBillType()).isSameAs(billType);
            assertThat(mapped.getClosed()).isFalse();
            verify(documentService).createDocument(docParamsCaptor.capture());
            var params = docParamsCaptor.getValue();
            assertThat(params.getPath()).isEqualTo("bills/water");
            assertThat(params.getReferenceDayCalendarDate()).isEqualTo(REFERENCE);
            assertThat(params.getMultipartFile()).isSameAs(dto.getMultipartFile());
            assertThat(params.getDocumentType()).isEqualTo(DocumentType.BILLET);
            assertThat(params.getReferenceType()).isEqualTo(ReferenceType.MONTH);
            verify(repository).save(mapped);
            verify(repository).countByBudgetId(PARENT_ID);
            verify(toEntityMapper).apply(dto);
            verify(budgetService).getBudgetValues(PARENT_ID);
            verifyNoMoreInteractions(repository, toEntityMapper, budgetService, documentService);
            verifyNoInteractions(billTypeService, billBalanceService);
        }

        @Test
        @DisplayName("without a document date, should leave the document day empty")
        void givenNoDocumentDate_shouldLeaveDocumentDateEmpty() {
            var dto = openDto(null);
            var mapped = Bill.builder().build();
            when(toEntityMapper.apply(dto)).thenReturn(mapped);
            when(repository.countByBudgetId(PARENT_ID)).thenReturn(0L);
            when(budgetService.getBudgetValues(PARENT_ID)).thenReturn(budgetValues(false));
            when(documentService.createDocument(any(BillDocParams.class))).thenReturn(new Document());
            stubDay(REFERENCE, 20240301);
            stubDay(DUE, 20240320);
            when(entityManager.getReference(BillType.class, BILL_TYPE_ID)).thenReturn(BillType.builder().build());
            when(repository.save(mapped)).thenReturn(saved(null));

            service.save(dto, null);

            assertThat(mapped.getDocumentDate()).isNull();
            assertThat(mapped.getReferenceDay()).isEqualTo(new Day(20240301));
            assertThat(mapped.getDueDate()).isEqualTo(new Day(20240320));
        }

        @ParameterizedTest(name = "[{index}] {0} bills already opened for the budget")
        @ValueSource(longs = {1, 3})
        @DisplayName("when the budget already has a bill, should fail and neither store a document nor persist")
        void givenBillAlreadyOpened_shouldFail(long count) {
            var dto = openDto(DOCUMENT_DATE);
            when(toEntityMapper.apply(dto)).thenReturn(Bill.builder().build());
            when(repository.countByBudgetId(PARENT_ID)).thenReturn(count);

            assertThatThrownBy(() -> service.save(dto, null))
                    .satisfies(e -> assertErrorCode(e, ErrorCode.BILL_ALREADY_OPENED));

            verify(repository).countByBudgetId(PARENT_ID);
            verifyNoMoreInteractions(repository);
            verifyNoInteractions(documentService, budgetService, entityManager, dayService);
        }

        @ParameterizedTest(name = "[{index}] paid={0}")
        @NullSource
        @ValueSource(booleans = true)
        @DisplayName("when the budget is not unpaid, should fail and neither store a document nor persist")
        void givenBudgetNotUnpaid_shouldFail(Boolean paid) {
            var dto = openDto(DOCUMENT_DATE);
            when(toEntityMapper.apply(dto)).thenReturn(Bill.builder().build());
            when(repository.countByBudgetId(PARENT_ID)).thenReturn(0L);
            when(budgetService.getBudgetValues(PARENT_ID)).thenReturn(budgetValues(paid));

            assertThatThrownBy(() -> service.save(dto, null))
                    .satisfies(e -> assertErrorCode(e, ErrorCode.BILL_ALREADY_PAID));

            verify(budgetService).getBudgetValues(PARENT_ID);
            verify(repository).countByBudgetId(PARENT_ID);
            verifyNoMoreInteractions(repository, budgetService);
            verifyNoInteractions(documentService, entityManager, dayService);
        }
    }

    @Nested
    @DisplayName("save a closed bill")
    class SaveCloseBill {

        private final BillCloseDto closeDto = BillCloseDto.builder().billId(ID).paymentDateCalendarDate(PAYMENT)
                .paymentType(PaymentType.PIX).sourceId(SOURCE_ID).multipartFile(upload()).build();

        private Bill openBill(Boolean closed) {
            var referenceDay = new Day(20240301);
            referenceDay.setCalendarDate(REFERENCE);
            return Bill.builder().id(ID).closed(closed).billType(BillType.builder().id(BILL_TYPE_ID).build())
                    .referenceDay(referenceDay).referenceType(ReferenceType.MONTH).build();
        }

        @Test
        @DisplayName("should store the receipt, save the balance, close the bill and mark its budget as paid")
        void givenOpenBill_shouldCloseIt() {
            var bill = openBill(false);
            var receipt = new Document();
            var balance = Balance.builder().build();
            var budget = Budget.builder().id(PARENT_ID).build();
            when(repository.get(ID)).thenReturn(Optional.of(bill));
            when(billTypeService.getValues(BILL_TYPE_ID)).thenReturn(new BillTypeValuesDto("bills/water", CATEGORY_ID));
            when(documentService.createDocument(any(BillDocParams.class))).thenReturn(receipt);
            when(billBalanceService.saveBalance(any(BillBalanceParams.class))).thenReturn(balance);
            stubDay(PAYMENT, 20240318);
            when(repository.save(bill)).thenReturn(saved(budget));

            var result = service.save(closeDto, null);

            assertThat(result).isEqualTo(SAVED_ID.toString());
            assertThat(bill.getName()).isEqualTo("bogus");
            assertThat(bill.getPaymentDate()).isEqualTo(new Day(20240318));
            assertThat(bill.getReceipt()).isSameAs(receipt);
            assertThat(bill.getClosed()).isTrue();
            assertThat(bill.getBalance()).isSameAs(balance);
            verify(documentService).createDocument(docParamsCaptor.capture());
            var docParams = docParamsCaptor.getValue();
            assertThat(docParams.getPath()).isEqualTo("bills/water");
            assertThat(docParams.getReferenceDayCalendarDate()).isEqualTo(REFERENCE);
            assertThat(docParams.getMultipartFile()).isSameAs(closeDto.getMultipartFile());
            assertThat(docParams.getDocumentType()).isEqualTo(DocumentType.VOUCHER);
            assertThat(docParams.getReferenceType()).isEqualTo(ReferenceType.MONTH);
            verify(billBalanceService).saveBalance(balanceParamsCaptor.capture());
            var balanceParams = balanceParamsCaptor.getValue();
            assertThat(balanceParams.getBillCloseDto()).isSameAs(closeDto);
            assertThat(balanceParams.getBill()).isSameAs(bill);
            assertThat(balanceParams.getCategoryId()).isEqualTo(CATEGORY_ID);
            InOrder order = inOrder(repository, budgetService);
            order.verify(repository).save(bill);
            order.verify(budgetService).update(patchCaptor.capture(), eq(PARENT_ID));
            assertThat(patchCaptor.getValue()).usingRecursiveComparison().isEqualTo(BudgetPatchDto.of(true));
            verify(repository).get(ID);
            verify(billTypeService).getValues(BILL_TYPE_ID);
            verifyNoMoreInteractions(repository, budgetService, billTypeService, documentService, billBalanceService);
            verifyNoInteractions(toEntityMapper);
        }

        @Test
        @DisplayName("without a payment date, should leave the payment day empty")
        void givenNoPaymentDate_shouldLeavePaymentDateEmpty() {
            var bill = openBill(false);
            var noDate = BillCloseDto.builder().billId(ID).paymentDateCalendarDate(null).multipartFile(upload())
                    .build();
            when(repository.get(ID)).thenReturn(Optional.of(bill));
            when(billTypeService.getValues(BILL_TYPE_ID)).thenReturn(new BillTypeValuesDto("bills/water", CATEGORY_ID));
            when(documentService.createDocument(any(BillDocParams.class))).thenReturn(new Document());
            when(billBalanceService.saveBalance(any(BillBalanceParams.class))).thenReturn(Balance.builder().build());
            when(repository.save(bill)).thenReturn(saved(Budget.builder().id(PARENT_ID).build()));

            service.save(noDate, null);

            assertThat(bill.getPaymentDate()).isNull();
            verifyNoInteractions(dayService, entityManager);
        }

        @Test
        @DisplayName("when the bill does not exist, should fail with not found and do nothing else")
        void givenUnknownBill_shouldThrowNotFound() {
            when(repository.get(ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.save(closeDto, null))
                    .satisfies(e -> assertErrorCode(e, ErrorCode.ENTITY_DOES_NOT_EXIST));

            verify(repository).get(ID);
            verifyNoMoreInteractions(repository);
            verifyNoInteractions(documentService, billBalanceService, billTypeService, budgetService);
        }

        @ParameterizedTest(name = "[{index}] closed={0}")
        @NullSource
        @ValueSource(booleans = true)
        @DisplayName("when the bill is not open, should fail and neither store a receipt nor save a balance")
        void givenBillNotOpen_shouldFail(Boolean closed) {
            when(repository.get(ID)).thenReturn(Optional.of(openBill(closed)));

            assertThatThrownBy(() -> service.save(closeDto, null))
                    .satisfies(e -> assertErrorCode(e, ErrorCode.BILL_ALREADY_CLOSED));

            verify(repository).get(ID);
            verifyNoMoreInteractions(repository);
            verifyNoInteractions(documentService, billBalanceService, billTypeService, budgetService);
        }
    }

    @Nested
    @DisplayName("save an unsupported dto")
    class SaveUnsupported {

        @Test
        @DisplayName("with a BillSaveDto, should fail as not implemented")
        void givenBillSaveDto_shouldThrowUnsupportedOperation() {
            var dto = BillSaveDto.builder().build();

            assertThatThrownBy(() -> service.save(dto, null))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessage("Saving a bill is not implemented yet");

            verifyNoInteractions(repository, documentService, billBalanceService, billTypeService, budgetService);
        }

        @Test
        @DisplayName("with any other dto, should fail naming the dto type")
        void givenOtherDto_shouldThrowIllegalArgument() {
            var dto = IncomeSourceSaveDto.builder().build();

            assertThatThrownBy(() -> service.save(dto, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Unsupported DTO type: IncomeSourceSaveDto");

            verifyNoInteractions(repository, documentService, billBalanceService, billTypeService, budgetService);
        }
    }

    @Nested
    @DisplayName("getById")
    class GetById {

        private DocumentDto document(UUID id) {
            return new DocumentDto(id, "doc.pdf", "bill/water", "application/pdf", 5L, "hash");
        }

        @Test
        @DisplayName("with a bill document and an empty receipt, should expose the document storage path and drop the receipt")
        void givenDocumentAndEmptyReceipt_shouldKeepOnlyDocument() {
            var dto = new BillDto();
            dto.setBillDocument(document(DOCUMENT_ID));
            dto.setReceipt(document(null));
            when(repository.getSingle(ID)).thenReturn(dto);

            var result = service.getById(ID);

            assertThat(result).isSameAs(dto);
            assertThat(result.getBillDocument().getStoragePath()).isEqualTo(STORAGE);
            assertThat(result.getBillDocument().getId()).isEqualTo(DOCUMENT_ID);
            assertThat(result.getReceipt()).isNull();
            verify(repository).getSingle(ID);
            verifyNoMoreInteractions(repository);
        }

        @Test
        @DisplayName("with an empty bill document and a receipt, should drop the document and expose the receipt storage path")
        void givenEmptyDocumentAndReceipt_shouldKeepOnlyReceipt() {
            var dto = new BillDto();
            dto.setBillDocument(document(null));
            dto.setReceipt(document(RECEIPT_ID));
            when(repository.getSingle(ID)).thenReturn(dto);

            var result = service.getById(ID);

            assertThat(result.getBillDocument()).isNull();
            assertThat(result.getReceipt().getStoragePath()).isEqualTo(STORAGE);
            assertThat(result.getReceipt().getId()).isEqualTo(RECEIPT_ID);
        }

        @Test
        @DisplayName("without documents, should keep both null")
        void givenNoDocuments_shouldKeepNull() {
            var dto = new BillDto();
            when(repository.getSingle(ID)).thenReturn(dto);

            var result = service.getById(ID);

            assertThat(result.getBillDocument()).isNull();
            assertThat(result.getReceipt()).isNull();
        }

        @Test
        @DisplayName("when the bill does not exist, should fail with not found")
        void givenUnknownBill_shouldThrowNotFound() {
            when(repository.getSingle(ID)).thenReturn(null);

            assertThatThrownBy(() -> service.getById(ID))
                    .satisfies(e -> assertErrorCode(e, ErrorCode.ENTITY_DOES_NOT_EXIST));

            verify(repository).getSingle(ID);
            verifyNoMoreInteractions(repository);
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAll {

        @Test
        @DisplayName("should query with the bill predicate through the custom query")
        void shouldUseBillPredicate() {
            var filter = BillFilter.builder().active(true).search("abc").closed(false).build();
            var pageable = PageRequest.of(0, 10);
            var page = new PageImpl<>(List.of(new BillDto()), pageable, 1);
            when(repository.getAll(BillPredicateBuilder.all(filter), pageable)).thenReturn(page);

            var result = service.getAll(pageable, filter);

            assertThat(result).isSameAs(page);
            verify(repository).getAll(BillPredicateBuilder.all(filter), pageable);
            verifyNoMoreInteractions(repository);
        }
    }

    @Nested
    @DisplayName("getOpenBills")
    class GetOpenBills {

        @Test
        @DisplayName("should map each open bill to a select item valued by its id")
        void shouldMapToSelectItems() {
            when(repository.getOpenBills((short) 2024, (short) 3))
                    .thenReturn(List.of(new ItemDto(ID, "Water"), new ItemDto(PARENT_ID, "Rent")));

            var result = service.getOpenBills((short) 2024, (short) 3);

            assertThat(result).usingRecursiveComparison().isEqualTo(List.of(
                    SelectItem.of("Water", ID.toString()), SelectItem.of("Rent", PARENT_ID.toString())));
            verify(repository).getOpenBills((short) 2024, (short) 3);
            verifyNoMoreInteractions(repository);
        }
    }
}
