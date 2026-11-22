package com.onurbcd.cli.persistency.repository;

import com.onurbcd.cli.dto.document.DocumentDto;
import com.onurbcd.cli.enums.BalanceType;
import com.onurbcd.cli.enums.PaymentType;
import com.onurbcd.cli.enums.SourceType;
import com.onurbcd.cli.param.SequenceParam;
import com.onurbcd.cli.persistency.entity.Balance;
import com.onurbcd.cli.persistency.entity.Document;
import com.onurbcd.cli.persistency.entity.QBalance;
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

class BalanceRepositoryIT extends AbstractRepositoryIT {

    private static final QBalance BALANCE = QBalance.balance;
    private static final LocalDate JAN_10 = LocalDate.of(2025, 1, 10);
    private static final LocalDate JAN_20 = LocalDate.of(2025, 1, 20);
    private static final LocalDate FEB_10 = LocalDate.of(2025, 2, 10);

    @Autowired
    private BalanceRepository repository;

    private UUID sourceId;
    private UUID categoryId;

    private UUID balance(LocalDate date, int sequence, BalanceType type, String amount) {
        if (sourceId == null) {
            sourceId = fixtures.source(fixtures.incomeSource("Salary", true), "Wallet",
                    SourceType.USABLE, BigDecimal.TEN);
            categoryId = fixtures.category("Food", null, 1, true);
        }

        var dayId = jdbc.queryForList("select id from day where calendar_date = ?", Integer.class, date)
                .stream().findFirst().orElseGet(() -> fixtures.day(date));

        return fixtures.balance(dayId, sourceId, categoryId, sequence, type, new BigDecimal(amount));
    }

    private static SequenceParam param(int year, int month, int sequence, int target, BalanceType type) {
        return SequenceParam.of((short) year, (short) month, (short) sequence, (short) target, type);
    }

    private Short sequenceOf(UUID id) {
        return jdbc.queryForObject("select sequence from balance where id = ?", Short.class, id);
    }

    @Nested
    @DisplayName("deleteUsingId / updateActive")
    class DeleteAndActive {

        @Test
        void givenExistingId_whenDelete_shouldRemoveOnlyThatRow() {
            var target = balance(JAN_10, 1, BalanceType.INCOME, "10");
            var other = balance(JAN_10, 2, BalanceType.INCOME, "10");

            assertThat(repository.deleteUsingId(target)).isEqualTo(1);
            assertThat(jdbc.queryForList("select id from balance", UUID.class)).containsExactly(other);
        }

        @Test
        void givenUnknownId_whenDelete_shouldReturnZero() {
            balance(JAN_10, 1, BalanceType.INCOME, "10");

            assertThat(repository.deleteUsingId(UUID.randomUUID())).isZero();
            assertThat(jdbc.queryForObject("select count(*) from balance", Long.class)).isEqualTo(1);
        }

        @Test
        void givenExistingId_whenUpdateActive_shouldChangeOnlyThatRow() {
            var target = balance(JAN_10, 1, BalanceType.INCOME, "10");
            var other = balance(JAN_10, 2, BalanceType.INCOME, "10");

            assertThat(repository.updateActive(target, false)).isEqualTo(1);
            assertThat(active(target)).isFalse();
            assertThat(active(other)).isTrue();
        }

        @Test
        void givenUnknownId_whenUpdateActive_shouldReturnZero() {
            assertThat(repository.updateActive(UUID.randomUUID(), false)).isZero();
        }

        private Boolean active(UUID id) {
            return jdbc.queryForObject("select active from balance where id = ?", Boolean.class, id);
        }
    }

    @Nested
    @DisplayName("sequence queries")
    class Sequences {

        @Test
        void givenBalancesInSeveralMonthsAndTypes_whenGetMaxSequence_shouldConsiderMonthAndType() {
            balance(JAN_10, 1, BalanceType.INCOME, "10");
            balance(JAN_20, 4, BalanceType.INCOME, "10");
            balance(JAN_10, 9, BalanceType.OUTCOME, "10");
            balance(FEB_10, 8, BalanceType.INCOME, "10");

            assertThat(repository.getMaxSequence(param(2025, 1, 0, 0, BalanceType.INCOME))).isEqualTo((short) 4);
            assertThat(repository.getMaxSequence(param(2025, 1, 0, 0, BalanceType.OUTCOME))).isEqualTo((short) 9);
        }

        @Test
        void givenNoBalanceInMonth_whenGetMaxSequence_shouldReturnNull() {
            balance(JAN_10, 1, BalanceType.INCOME, "10");

            assertThat(repository.getMaxSequence(param(2025, 3, 0, 0, BalanceType.INCOME))).isNull();
        }

        @Test
        void givenSequence_whenExistsSequence_shouldMatchMonthSequenceAndType() {
            balance(JAN_10, 2, BalanceType.INCOME, "10");

            assertThat(repository.existsSequence(param(2025, 1, 2, 0, BalanceType.INCOME))).isTrue();
            assertThat(repository.existsSequence(param(2025, 1, 2, 0, BalanceType.OUTCOME))).isNull();
            assertThat(repository.existsSequence(param(2025, 1, 3, 0, BalanceType.INCOME))).isNull();
            assertThat(repository.existsSequence(param(2025, 2, 2, 0, BalanceType.INCOME))).isNull();
        }

        @Test
        void givenSequence_whenUpdateSequence_shouldMoveOnlyThatMonthAndType() {
            var moved = balance(JAN_10, 2, BalanceType.INCOME, "10");
            var otherSequence = balance(JAN_10, 3, BalanceType.INCOME, "10");
            var otherType = balance(JAN_10, 2, BalanceType.OUTCOME, "10");
            var otherMonth = balance(FEB_10, 2, BalanceType.INCOME, "10");

            repository.updateSequence(param(2025, 1, 2, 7, BalanceType.INCOME));

            assertThat(sequenceOf(moved)).isEqualTo((short) 7);
            assertThat(sequenceOf(otherSequence)).isEqualTo((short) 3);
            assertThat(sequenceOf(otherType)).isEqualTo((short) 2);
            assertThat(sequenceOf(otherMonth)).isEqualTo((short) 2);
        }

        @Test
        void givenSequence_whenCountNextSequences_shouldCountGreaterOnesOfMonthAndType() {
            balance(JAN_10, 1, BalanceType.INCOME, "10");
            balance(JAN_10, 2, BalanceType.INCOME, "10");
            balance(JAN_20, 3, BalanceType.INCOME, "10");
            balance(JAN_10, 9, BalanceType.OUTCOME, "10");
            balance(FEB_10, 9, BalanceType.INCOME, "10");

            assertThat(repository.countNextSequences(param(2025, 1, 1, 0, BalanceType.INCOME))).isEqualTo(2);
            assertThat(repository.countNextSequences(param(2025, 1, 3, 0, BalanceType.INCOME))).isZero();
        }
    }

    @Nested
    @DisplayName("documents")
    class Documents {

        @Test
        void givenDocuments_whenGetDocumentsById_shouldProjectOnlyThatBalanceDocuments() {
            var target = balance(JAN_10, 1, BalanceType.INCOME, "10");
            var other = balance(JAN_10, 2, BalanceType.INCOME, "10");
            var doc = fixtures.document("a.pdf");
            fixtures.balanceDocument(target, doc);
            fixtures.balanceDocument(other, fixtures.document("b.pdf"));

            var result = repository.getDocumentsById(target);

            assertThat(result).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new DocumentDto(doc, "a.pdf", "/docs/a.pdf", "application/pdf", 1234L, "hash-a.pdf"));
        }

        @Test
        void givenBalanceWithoutDocuments_whenGetDocumentsById_shouldProjectOneEmptyDto() {
            var target = balance(JAN_10, 1, BalanceType.INCOME, "10");

            var result = repository.getDocumentsById(target);

            assertThat(result).singleElement().satisfies(dto -> assertThat(dto.isNotEmpty()).isFalse());
        }

        @Test
        void givenUnknownBalance_whenGetDocumentsById_shouldReturnEmpty() {
            assertThat(repository.getDocumentsById(UUID.randomUUID())).isEmpty();
        }

        @Test
        void givenDocuments_whenGetDocuments_shouldReturnOnlyThatBalanceDocuments() {
            var target = balance(JAN_10, 1, BalanceType.INCOME, "10");
            var doc = fixtures.document("a.pdf");
            fixtures.balanceDocument(target, doc);
            fixtures.balanceDocument(balance(JAN_10, 2, BalanceType.INCOME, "10"), fixtures.document("b.pdf"));
            clear();

            var result = repository.getDocuments(target);

            assertThat(result).extracting(Document::getId, Document::getName).containsExactly(Tuple.tuple(doc, "a.pdf"));
        }

        @Test
        void givenUnknownBalance_whenGetDocuments_shouldReturnEmpty() {
            assertThat(repository.getDocuments(UUID.randomUUID())).isEmpty();
        }
    }

    @Nested
    @DisplayName("get")
    class Get {

        @Test
        void givenId_whenGet_shouldFetchDayAndSourceAndCategory() {
            var target = balance(JAN_10, 1, BalanceType.INCOME, "10.5");
            clear();

            var result = repository.get(target);

            assertThat(result).isPresent();
            Balance balance = result.get();
            assertThat(balance.getId()).isEqualTo(target);
            assertThat(balance.getAmount()).isEqualByComparingTo("10.5");
            assertThat(Hibernate.isInitialized(balance.getDay())).isTrue();
            assertThat(Hibernate.isInitialized(balance.getSource())).isTrue();
            assertThat(Hibernate.isInitialized(balance.getCategory())).isTrue();
            assertThat(balance.getDay().getCalendarDate()).isEqualTo(JAN_10);
            assertThat(balance.getSource().getId()).isEqualTo(sourceId);
            assertThat(balance.getCategory().getId()).isEqualTo(categoryId);
        }

        @Test
        void givenUnknownId_whenGet_shouldReturnEmpty() {
            assertThat(repository.get(UUID.randomUUID())).isEmpty();
        }
    }

    @Nested
    @DisplayName("getItems")
    class GetItems {

        @Test
        void givenAnyId_whenGetItems_shouldUseTheDefaultEmptyList() {
            assertThat(repository.getItems(null)).isEmpty();
            assertThat(repository.getItems(UUID.randomUUID())).isEmpty();
        }
    }

    @Nested
    @DisplayName("custom queries (BalanceRepositoryImpl)")
    class Custom {

        @Test
        void givenId_whenGetSingle_shouldProjectTheRowWithDaySourceAndCategory() {
            var target = balance(JAN_10, 3, BalanceType.OUTCOME, "10.5");
            balance(JAN_10, 4, BalanceType.OUTCOME, "99");

            var result = repository.getSingle(target);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(target);
            assertThat(result.getActive()).isTrue();
            assertThat(result.getSequence()).isEqualTo((short) 3);
            assertThat(result.getDayId()).isEqualTo(20250110);
            assertThat(result.getDayCalendarDate()).isEqualTo(JAN_10);
            assertThat(result.getSourceId()).isEqualTo(sourceId);
            assertThat(result.getSourceName()).isEqualTo("Wallet");
            assertThat(result.getCategoryId()).isEqualTo(categoryId);
            assertThat(result.getCategoryName()).isEqualTo("Food");
            assertThat(result.getAmount()).isEqualByComparingTo("10.5");
            assertThat(result.getCode()).isEqualTo("code-3");
            assertThat(result.getDescription()).isEqualTo("description 3");
            assertThat(result.getBalanceType()).isEqualTo(BalanceType.OUTCOME);
            assertThat(result.getPaymentType()).isEqualTo(PaymentType.CASH);
            assertThat(result.getCreatedDate()).isEqualTo(RepositoryFixtures.CREATED);
        }

        @Test
        void givenUnknownId_whenGetSingle_shouldReturnNull() {
            balance(JAN_10, 1, BalanceType.INCOME, "10");

            assertThat(repository.getSingle(UUID.randomUUID())).isNull();
        }

        @Test
        void givenPredicate_whenGetAll_shouldReturnOnlyMatchingRows() {
            balance(JAN_10, 1, BalanceType.INCOME, "10");
            balance(JAN_10, 2, BalanceType.OUTCOME, "20");

            var result = repository.getAll(BALANCE.balanceType.eq(BalanceType.OUTCOME));

            assertThat(result).extracting("sequence", "sourceName", "categoryName")
                    .containsExactly(Tuple.tuple((short) 2, "Wallet", "Food"));
        }

        @Test
        void givenPageable_whenGetAll_shouldPageSortAndCount() {
            balance(JAN_10, 1, BalanceType.INCOME, "10");
            balance(JAN_10, 2, BalanceType.INCOME, "20");
            balance(JAN_10, 3, BalanceType.INCOME, "30");

            var first = repository.getAll(new BooleanBuilder(), PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "sequence")));
            var second = repository.getAll(new BooleanBuilder(), PageRequest.of(1, 2, Sort.by(Sort.Direction.DESC, "sequence")));

            assertThat(first.getTotalElements()).isEqualTo(3);
            assertThat(first.getContent()).extracting("sequence").containsExactly((short) 3, (short) 2);
            assertThat(second.getContent()).extracting("sequence").containsExactly((short) 1);
        }

        @Test
        void givenPredicate_whenGetSum_shouldSumTheMatchingAmounts() {
            balance(JAN_10, 1, BalanceType.INCOME, "10.5");
            balance(JAN_10, 2, BalanceType.INCOME, "20.25");
            balance(JAN_10, 3, BalanceType.OUTCOME, "100");

            assertThat(repository.getSum(BALANCE.balanceType.eq(BalanceType.INCOME))).isEqualByComparingTo("30.75");
        }

        @Test
        void givenNoMatch_whenGetSum_shouldReturnNull() {
            assertThat(repository.getSum(BALANCE.balanceType.eq(BalanceType.INCOME))).isNull();
        }
    }
}
