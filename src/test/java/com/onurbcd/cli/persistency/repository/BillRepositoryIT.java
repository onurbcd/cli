package com.onurbcd.cli.persistency.repository;

import com.onurbcd.cli.dto.ItemDto;
import com.onurbcd.cli.dto.document.DocumentDto;
import com.onurbcd.cli.enums.BalanceType;
import com.onurbcd.cli.enums.DocumentType;
import com.onurbcd.cli.enums.PaymentType;
import com.onurbcd.cli.enums.ReferenceType;
import com.onurbcd.cli.enums.SourceType;
import com.onurbcd.cli.persistency.entity.Bill;
import com.onurbcd.cli.persistency.entity.QBill;
import com.querydsl.core.BooleanBuilder;
import org.assertj.core.groups.Tuple;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BillRepositoryIT extends AbstractRepositoryIT {

    private static final QBill BILL = QBill.bill;
    private static final LocalDate REFERENCE = LocalDate.of(2025, 1, 1);
    private static final LocalDate DUE = LocalDate.of(2025, 1, 15);

    @Autowired
    private BillRepository repository;

    private UUID billTypeId;
    private Integer referenceDayId;
    private Integer dueDayId;

    /**
     * A bill needs its own budget (unique constraint), so each call creates one.
     */
    private UUID bill(String budgetName, int year, int month, int sequence, boolean closed) {
        if (billTypeId == null) {
            billTypeId = fixtures.billType("Rent", fixtures.category("Home", null, 1, true), "home/rent");
            referenceDayId = fixtures.day(REFERENCE);
            dueDayId = fixtures.day(DUE);
        }

        var budget = fixtures.budget(billTypeId, budgetName, year, month, sequence, new BigDecimal("150.5"), false);

        return fixtures.bill(budget, billTypeId, referenceDayId, dueDayId, closed);
    }

    private UUID budgetOf(UUID billId) {
        return jdbc.queryForObject("select budget_id from bill where id = ?", UUID.class, billId);
    }

    @Nested
    @DisplayName("deleteUsingId / updateActive")
    class DeleteAndActive {

        @Test
        void givenExistingId_whenDelete_shouldRemoveOnlyThatRow() {
            var target = bill("A", 2025, 1, 1, false);
            var other = bill("B", 2025, 1, 2, false);

            assertThat(repository.deleteUsingId(target)).isEqualTo(1);
            assertThat(jdbc.queryForList("select id from bill", UUID.class)).containsExactly(other);
        }

        @Test
        void givenUnknownId_whenDelete_shouldReturnZero() {
            bill("A", 2025, 1, 1, false);

            assertThat(repository.deleteUsingId(UUID.randomUUID())).isZero();
            assertThat(jdbc.queryForObject("select count(*) from bill", Long.class)).isEqualTo(1);
        }

        @Test
        void givenExistingId_whenUpdateActive_shouldChangeOnlyThatRow() {
            var target = bill("A", 2025, 1, 1, false);
            var other = bill("B", 2025, 1, 2, false);

            assertThat(repository.updateActive(target, false)).isEqualTo(1);
            assertThat(active(target)).isFalse();
            assertThat(active(other)).isTrue();
        }

        @Test
        void givenUnknownId_whenUpdateActive_shouldReturnZero() {
            assertThat(repository.updateActive(UUID.randomUUID(), false)).isZero();
        }

        private Boolean active(UUID id) {
            return jdbc.queryForObject("select active from bill where id = ?", Boolean.class, id);
        }
    }

    @Nested
    @DisplayName("get")
    class Get {

        @Test
        void givenBillWithEverything_whenGet_shouldFetchTheWholeGraph() {
            var target = bill("A", 2025, 1, 1, true);
            var balance = fullBalance();
            var paymentDayId = fixtures.day(LocalDate.of(2025, 1, 16));
            var documentDayId = fixtures.day(LocalDate.of(2025, 1, 5));
            fixtures.withBillExtras(target, documentDayId, paymentDayId, null, null, balance);
            clear();

            var result = repository.get(target);

            assertThat(result).isPresent();
            Bill bill = result.get();
            assertThat(bill.getId()).isEqualTo(target);
            assertThat(Hibernate.isInitialized(bill.getReferenceDay())).isTrue();
            assertThat(Hibernate.isInitialized(bill.getDueDate())).isTrue();
            assertThat(Hibernate.isInitialized(bill.getBillType())).isTrue();
            assertThat(Hibernate.isInitialized(bill.getBudget())).isTrue();
            assertThat(Hibernate.isInitialized(bill.getDocumentDate())).isTrue();
            assertThat(Hibernate.isInitialized(bill.getPaymentDate())).isTrue();
            assertThat(Hibernate.isInitialized(bill.getBalance())).isTrue();
            assertThat(Hibernate.isInitialized(bill.getBalance().getSource())).isTrue();
            assertThat(bill.getPaymentDate().getId()).isEqualTo(paymentDayId);
            assertThat(bill.getBalance().getId()).isEqualTo(balance);
        }

        @Test
        void givenBillWithoutOptionals_whenGet_shouldKeepItBecauseThoseJoinsAreLeft() {
            var target = bill("A", 2025, 1, 1, false);
            clear();

            var result = repository.get(target);

            assertThat(result).isPresent();
            assertThat(result.get().getDocumentDate()).isNull();
            assertThat(result.get().getPaymentDate()).isNull();
            assertThat(result.get().getBalance()).isNull();
        }

        @Test
        void givenUnknownId_whenGet_shouldReturnEmpty() {
            assertThat(repository.get(UUID.randomUUID())).isEmpty();
        }
    }

    @Nested
    @DisplayName("countByBudgetId / getOpenBills / getItems")
    class Queries {

        @Test
        void givenBudget_whenCountByBudgetId_shouldCountItsBills() {
            var target = bill("A", 2025, 1, 1, false);
            bill("B", 2025, 1, 2, false);

            assertThat(repository.countByBudgetId(budgetOf(target))).isEqualTo(1);
            assertThat(repository.countByBudgetId(UUID.randomUUID())).isZero();
        }

        @Test
        void givenBills_whenGetOpenBills_shouldReturnOpenOnesOfTheMonthOrderedByBudgetName() {
            var zulu = bill("Zulu", 2025, 1, 1, false);
            var alpha = bill("Alpha", 2025, 1, 2, false);
            bill("Closed", 2025, 1, 3, true);
            bill("Other month", 2025, 2, 1, false);
            bill("Other year", 2024, 1, 1, false);

            var result = repository.getOpenBills((short) 2025, (short) 1);

            assertThat(result).usingRecursiveFieldByFieldElementComparator()
                    .containsExactly(new ItemDto(alpha, "Alpha"), new ItemDto(zulu, "Zulu"));
        }

        @Test
        void givenNoOpenBill_whenGetOpenBills_shouldReturnEmpty() {
            bill("Closed", 2025, 1, 3, true);

            assertThat(repository.getOpenBills((short) 2025, (short) 1)).isEmpty();
        }

        @Test
        void givenAnyId_whenGetItems_shouldUseTheDefaultEmptyList() {
            assertThat(repository.getItems(null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("custom queries (BillRepositoryImpl)")
    class Custom {

        @Test
        void givenBillWithoutOptionals_whenGetSingle_shouldProjectTheMandatoryColumns() {
            var target = bill("Budget A", 2025, 1, 1, false);
            bill("Budget B", 2025, 1, 2, false);

            var result = repository.getSingle(target);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(target);
            assertThat(result.getActive()).isTrue();
            assertThat(result.getReferenceDayId()).isEqualTo(referenceDayId);
            assertThat(result.getReferenceDayCalendarDate()).isEqualTo(REFERENCE);
            assertThat(result.getDueDateId()).isEqualTo(dueDayId);
            assertThat(result.getDueDateCalendarDate()).isEqualTo(DUE);
            assertThat(result.getValue()).isEqualByComparingTo("150.5");
            assertThat(result.getBillTypeId()).isEqualTo(billTypeId);
            assertThat(result.getBillTypeName()).isEqualTo("Rent");
            assertThat(result.getDocumentType()).isEqualTo(DocumentType.BILL);
            assertThat(result.getBudgetId()).isEqualTo(budgetOf(target));
            assertThat(result.getBudgetName()).isEqualTo("Budget A");
            assertThat(result.getReferenceType()).isEqualTo(ReferenceType.MONTH);
            assertThat(result.getClosed()).isFalse();
            assertThat(result.getDocumentDateId()).isNull();
            assertThat(result.getPaymentDateId()).isNull();
            assertThat(result.getBalanceId()).isNull();
            assertThat(result.getSourceId()).isNull();
            assertThat(result.getObservation()).isNull();
            assertThat(result.getInstallment()).isNull();
            assertThat(result.getBillDocument().isNotEmpty()).isFalse();
            assertThat(result.getReceipt().isNotEmpty()).isFalse();
            assertThat(result.getCreatedDate()).isEqualTo(RepositoryFixtures.CREATED);
        }

        @Test
        void givenBillWithEverything_whenGetSingle_shouldProjectOptionalColumnsAndDocuments() {
            var target = bill("Budget A", 2025, 1, 1, true);
            var balance = fullBalance();
            var billDocument = fixtures.document("bill.pdf");
            var receipt = fixtures.document("receipt.pdf");
            var documentDayId = fixtures.day(LocalDate.of(2025, 1, 5));
            var paymentDayId = fixtures.day(LocalDate.of(2025, 1, 16));
            fixtures.withBillExtras(target, documentDayId, paymentDayId, billDocument, receipt, balance);

            var result = repository.getSingle(target);

            assertThat(result).isNotNull();
            assertThat(result.getDocumentDateId()).isEqualTo(documentDayId);
            assertThat(result.getDocumentDateCalendarDate()).isEqualTo(LocalDate.of(2025, 1, 5));
            assertThat(result.getPaymentDateId()).isEqualTo(paymentDayId);
            assertThat(result.getPaymentDateCalendarDate()).isEqualTo(LocalDate.of(2025, 1, 16));
            assertThat(result.getObservation()).isEqualTo("observation");
            assertThat(result.getInstallment()).isEqualTo((short) 3);
            assertThat(result.getClosed()).isTrue();
            assertThat(result.getBalanceId()).isEqualTo(balance);
            assertThat(result.getPaymentType()).isEqualTo(PaymentType.CASH);
            assertThat(result.getSourceId()).isEqualTo(sourceOf(balance));
            assertThat(result.getBillDocumentId()).isEqualTo(billDocument);
            assertThat(result.getBillDocument()).usingRecursiveComparison().ignoringFields("storagePath")
                    .isEqualTo(documentDto(billDocument, "bill.pdf"));
            assertThat(result.getReceiptId()).isEqualTo(receipt);
            assertThat(result.getReceipt()).usingRecursiveComparison().ignoringFields("storagePath")
                    .isEqualTo(documentDto(receipt, "receipt.pdf"));
        }

        @Test
        void givenUnknownId_whenGetSingle_shouldReturnNull() {
            bill("A", 2025, 1, 1, false);

            assertThat(repository.getSingle(UUID.randomUUID())).isNull();
        }

        @Test
        void givenPredicate_whenGetAll_shouldReturnOnlyMatchingRows() {
            bill("A", 2025, 1, 1, false);
            bill("B", 2025, 1, 2, true);

            var result = repository.getAll(BILL.closed.isTrue());

            assertThat(result).extracting("budgetName", "closed").containsExactly(Tuple.tuple("B", true));
        }

        @Test
        void givenPageable_whenGetAll_shouldPageSortAndCount() {
            bill("A", 2025, 1, 1, false);
            bill("B", 2025, 1, 2, false);
            bill("C", 2025, 1, 3, false);

            var first = repository.getAll(new BooleanBuilder(), PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdDate", "id")));

            assertThat(first.getTotalElements()).isEqualTo(3);
            assertThat(first.getContent()).hasSize(2);
            assertThat(repository.getAll(new BooleanBuilder(), PageRequest.of(1, 2, Sort.by("id"))).getContent())
                    .hasSize(1);
        }

        @Test
        void whenGetSum_shouldReturnZero() {
            bill("A", 2025, 1, 1, false);

            assertThat(repository.getSum(new BooleanBuilder())).isEqualTo(BigDecimal.ZERO);
        }
    }

    private UUID fullBalance() {
        var source = fixtures.source(fixtures.incomeSource("Salary", true), "Wallet", SourceType.USABLE, BigDecimal.TEN);
        var category = fixtures.category("Food", null, 1, true);
        var day = fixtures.day(LocalDate.of(2025, 1, 14));

        return fixtures.balance(day, source, category, 1, BalanceType.OUTCOME, new BigDecimal("150.5"));
    }

    private UUID sourceOf(UUID balanceId) {
        return jdbc.queryForObject("select source_id from balance where id = ?", UUID.class, balanceId);
    }

    private static DocumentDto documentDto(UUID id, String name) {
        return new DocumentDto(id, name, "/docs/" + name, "application/pdf", 1234L,
                "hash-" + name);
    }
}
