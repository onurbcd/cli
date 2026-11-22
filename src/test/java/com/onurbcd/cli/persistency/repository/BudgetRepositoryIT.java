package com.onurbcd.cli.persistency.repository;

import com.onurbcd.cli.dto.budget.BudgetSumDto;
import com.onurbcd.cli.dto.budget.BudgetValuesDto;
import com.onurbcd.cli.dto.budget.MonthlyBudgetDto;
import com.onurbcd.cli.param.SequenceParam;
import com.onurbcd.cli.persistency.entity.QBudget;
import com.querydsl.core.BooleanBuilder;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BudgetRepositoryIT extends AbstractRepositoryIT {

    private static final QBudget BUDGET = QBudget.budget;
    private static final LocalDateTime MODIFIED = LocalDateTime.of(2026, 5, 6, 7, 8, 9);

    @Autowired
    private BudgetRepository repository;

    private UUID billTypeId;

    private UUID budget(String name, int year, int month, int sequence, String amount, boolean paid) {
        if (billTypeId == null) {
            billTypeId = fixtures.billType("Rent", fixtures.category("Home", null, 1, true), "home/rent");
        }

        return fixtures.budget(billTypeId, name, year, month, sequence, new BigDecimal(amount), paid);
    }

    private static SequenceParam param(int year, int month, int sequence, int target) {
        return SequenceParam.of((short) year, (short) month, (short) sequence, (short) target, null);
    }

    private Short sequenceOf(UUID id) {
        return jdbc.queryForObject("select sequence from budget where id = ?", Short.class, id);
    }

    @Nested
    @DisplayName("sequence queries")
    class Sequences {

        @Test
        void givenBudgetsOfSeveralMonths_whenGetMaxSequence_shouldConsiderOnlyTheMonth() {
            budget("A", 2025, 1, 1, "10", false);
            budget("B", 2025, 1, 5, "10", false);
            budget("C", 2025, 2, 9, "10", false);

            assertThat(repository.getMaxSequence(param(2025, 1, 0, 0))).isEqualTo((short) 5);
        }

        @Test
        void givenNoBudgetInMonth_whenGetMaxSequence_shouldReturnNull() {
            budget("A", 2025, 1, 1, "10", false);

            assertThat(repository.getMaxSequence(param(2025, 3, 0, 0))).isNull();
        }

        @Test
        void givenSequence_whenExistsSequence_shouldBeTrueOnlyWithinTheMonth() {
            budget("A", 2025, 1, 2, "10", false);

            assertThat(repository.existsSequence(param(2025, 1, 2, 0))).isTrue();
            assertThat(repository.existsSequence(param(2025, 1, 3, 0))).isNull();
            assertThat(repository.existsSequence(param(2025, 2, 2, 0))).isNull();
        }

        @Test
        void givenSequence_whenUpdateSequence_shouldMoveOnlyThatBudget() {
            var moved = budget("A", 2025, 1, 2, "10", false);
            var sameMonth = budget("B", 2025, 1, 3, "10", false);
            var otherMonth = budget("C", 2025, 2, 2, "10", false);

            repository.updateSequence(param(2025, 1, 2, 7));

            assertThat(sequenceOf(moved)).isEqualTo((short) 7);
            assertThat(sequenceOf(sameMonth)).isEqualTo((short) 3);
            assertThat(sequenceOf(otherMonth)).isEqualTo((short) 2);
        }

        @Test
        void givenSequence_whenCountNextSequences_shouldCountOnlyGreaterOnesInTheMonth() {
            budget("A", 2025, 1, 1, "10", false);
            budget("B", 2025, 1, 2, "10", false);
            budget("C", 2025, 1, 3, "10", false);
            budget("D", 2025, 2, 9, "10", false);

            assertThat(repository.countNextSequences(param(2025, 1, 1, 0))).isEqualTo(2);
            assertThat(repository.countNextSequences(param(2025, 1, 3, 0))).isZero();
        }
    }

    @Nested
    @DisplayName("getSumByMonth")
    class GetSumByMonth {

        @Test
        void givenPaidAndUnpaid_whenGetSumByMonth_shouldGroupByPaid() {
            budget("A", 2025, 1, 1, "10.5", true);
            budget("B", 2025, 1, 2, "20", true);
            budget("C", 2025, 1, 3, "5", false);
            budget("D", 2025, 2, 1, "999", false);

            var result = repository.getSumByMonth((short) 2025, (short) 1);

            assertThat(result).extracting(BudgetSumDto::getPaid, BudgetSumDto::getSize)
                    .containsExactlyInAnyOrder(Tuple.tuple(true, 2L), Tuple.tuple(false, 1L));
            assertThat(result).filteredOn(BudgetSumDto::getPaid).singleElement()
                    .extracting(BudgetSumDto::getAmount).satisfies(a -> assertThat(a).isEqualByComparingTo("30.5"));
            assertThat(result).filteredOn(dto -> !dto.getPaid()).singleElement()
                    .extracting(BudgetSumDto::getAmount).satisfies(a -> assertThat(a).isEqualByComparingTo("5"));
        }

        @Test
        void givenNoBudgetInMonth_whenGetSumByMonth_shouldReturnEmpty() {
            budget("A", 2025, 2, 1, "10", true);

            assertThat(repository.getSumByMonth((short) 2025, (short) 1)).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteAll(year, month)")
    class DeleteAll {

        @Test
        void givenBudgets_whenDeleteAll_shouldRemoveOnlyTheMonth() {
            budget("A", 2025, 1, 1, "10", false);
            budget("B", 2025, 1, 2, "10", false);
            var kept = budget("C", 2025, 2, 1, "10", false);

            var deleted = repository.deleteAll((short) 2025, (short) 1);

            assertThat(deleted).isEqualTo(2);
            assertThat(jdbc.queryForList("select id from budget", UUID.class)).containsExactly(kept);
        }

        @Test
        void givenNoBudgetInMonth_whenDeleteAll_shouldReturnZero() {
            budget("A", 2025, 2, 1, "10", false);

            assertThat(repository.deleteAll((short) 2025, (short) 1)).isZero();
        }
    }

    @Nested
    @DisplayName("updates")
    class Updates {

        @Test
        void givenBudget_whenUpdateActive_shouldChangeOnlyThatRow() {
            var target = budget("A", 2025, 1, 1, "10", false);
            var other = budget("B", 2025, 1, 2, "10", false);

            assertThat(repository.updateActive(target, false)).isEqualTo(1);
            assertThat(active(target)).isFalse();
            assertThat(active(other)).isTrue();
        }

        @Test
        void givenUnknownId_whenUpdateActive_shouldReturnZero() {
            assertThat(repository.updateActive(UUID.randomUUID(), false)).isZero();
        }

        @Test
        void givenBudget_whenUpdatePaid_shouldChangePaidAndModifiedDateOnly() {
            var target = budget("A", 2025, 1, 1, "10", false);
            var other = budget("B", 2025, 1, 2, "10", false);

            var updated = repository.updatePaid(target, true, MODIFIED);

            assertThat(updated).isEqualTo(1);
            assertThat(paid(target)).isTrue();
            assertThat(modified(target)).isEqualTo(MODIFIED);
            assertThat(active(target)).isTrue();
            assertThat(paid(other)).isFalse();
            assertThat(modified(other)).isEqualTo(RepositoryFixtures.CREATED);
        }

        @Test
        void givenUnknownId_whenUpdatePaid_shouldReturnZero() {
            assertThat(repository.updatePaid(UUID.randomUUID(), true, MODIFIED)).isZero();
        }

        @Test
        void givenBudget_whenUpdateActiveAndPaid_shouldChangeBothAndModifiedDate() {
            var target = budget("A", 2025, 1, 1, "10", false);
            var other = budget("B", 2025, 1, 2, "10", false);

            var updated = repository.updateActiveAndPaid(target, false, true, MODIFIED);

            assertThat(updated).isEqualTo(1);
            assertThat(active(target)).isFalse();
            assertThat(paid(target)).isTrue();
            assertThat(modified(target)).isEqualTo(MODIFIED);
            assertThat(active(other)).isTrue();
            assertThat(paid(other)).isFalse();
        }

        @Test
        void givenUnknownId_whenUpdateActiveAndPaid_shouldReturnZero() {
            assertThat(repository.updateActiveAndPaid(UUID.randomUUID(), false, true, MODIFIED)).isZero();
        }

        private Boolean active(UUID id) {
            return jdbc.queryForObject("select active from budget where id = ?", Boolean.class, id);
        }

        private Boolean paid(UUID id) {
            return jdbc.queryForObject("select paid from budget where id = ?", Boolean.class, id);
        }

        private LocalDateTime modified(UUID id) {
            return jdbc.queryForObject("select last_modified_date from budget where id = ?", LocalDateTime.class, id);
        }
    }

    @Nested
    @DisplayName("getBudgetValues / getMonthlyBudget")
    class Projections {

        @Test
        void givenId_whenGetBudgetValues_shouldProjectAmountBillTypeAndPaid() {
            var target = budget("A", 2025, 1, 1, "10.5", true);
            budget("B", 2025, 1, 2, "99", false);

            var result = repository.getBudgetValues(target);

            assertThat(result).isPresent();
            assertThat(result.get()).extracting(BudgetValuesDto::billTypeId, BudgetValuesDto::path, BudgetValuesDto::paid)
                    .containsExactly(billTypeId, "home/rent", true);
            assertThat(result.get().amount()).isEqualByComparingTo("10.5");
        }

        @Test
        void givenUnknownId_whenGetBudgetValues_shouldReturnEmpty() {
            assertThat(repository.getBudgetValues(UUID.randomUUID())).isEmpty();
        }

        @Test
        void givenBudgets_whenGetMonthlyBudget_shouldReturnTheMonthOrderedBySequence() {
            var second = budget("Second", 2025, 1, 2, "10", false);
            var first = budget("First", 2025, 1, 1, "10", false);
            budget("Other month", 2025, 2, 1, "10", false);

            List<MonthlyBudgetDto> result = repository.getMonthlyBudget((short) 2025, (short) 1);

            assertThat(result).containsExactly(
                    new MonthlyBudgetDto(first, "First", (short) 1), new MonthlyBudgetDto(second, "Second", (short) 2));
        }

        @Test
        void givenNoBudgetInMonth_whenGetMonthlyBudget_shouldReturnEmpty() {
            assertThat(repository.getMonthlyBudget((short) 2025, (short) 1)).isEmpty();
        }
    }

    @Nested
    @DisplayName("custom queries (BudgetRepositoryImpl)")
    class Custom {

        @Test
        void givenId_whenGetSingle_shouldProjectTheRowWithItsBillType() {
            var target = budget("A", 2025, 3, 4, "10.5", true);
            budget("B", 2025, 3, 5, "1", false);

            var result = repository.getSingle(target);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(target);
            assertThat(result.getName()).isEqualTo("A");
            assertThat(result.getActive()).isTrue();
            assertThat(result.getSequence()).isEqualTo((short) 4);
            assertThat(result.getRefYear()).isEqualTo((short) 2025);
            assertThat(result.getRefMonth()).isEqualTo((short) 3);
            assertThat(result.getBillTypeId()).isEqualTo(billTypeId);
            assertThat(result.getBillTypeName()).isEqualTo("Rent");
            assertThat(result.getAmount()).isEqualByComparingTo("10.5");
            assertThat(result.getPaid()).isTrue();
            assertThat(result.getCreatedDate()).isEqualTo(RepositoryFixtures.CREATED);
        }

        @Test
        void givenUnknownId_whenGetSingle_shouldReturnNull() {
            budget("A", 2025, 3, 4, "10", false);

            assertThat(repository.getSingle(UUID.randomUUID())).isNull();
        }

        @Test
        void givenPredicate_whenGetAll_shouldReturnOnlyMatchingRows() {
            budget("A", 2025, 1, 1, "10", false);
            budget("B", 2025, 2, 1, "10", false);

            var result = repository.getAll(BUDGET.refMonth.eq((short) 2));

            assertThat(result).extracting("name", "billTypeName").containsExactly(Tuple.tuple("B", "Rent"));
        }

        @Test
        void givenPageable_whenGetAll_shouldPageSortAndCount() {
            budget("A", 2025, 1, 1, "10", false);
            budget("B", 2025, 1, 2, "10", false);
            budget("C", 2025, 1, 3, "10", false);

            var first = repository.getAll(new BooleanBuilder(), PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "sequence")));
            var second = repository.getAll(new BooleanBuilder(), PageRequest.of(1, 2, Sort.by(Sort.Direction.DESC, "sequence")));

            assertThat(first.getTotalElements()).isEqualTo(3);
            assertThat(first.getContent()).extracting("name").containsExactly("C", "B");
            assertThat(second.getContent()).extracting("name").containsExactly("A");
        }

        @Test
        void whenGetSum_shouldReturnZero() {
            assertThat(repository.getSum(new BooleanBuilder())).isEqualTo(BigDecimal.ZERO);
        }
    }
}
