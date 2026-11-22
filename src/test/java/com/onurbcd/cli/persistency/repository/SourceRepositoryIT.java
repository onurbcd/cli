package com.onurbcd.cli.persistency.repository;

import com.onurbcd.cli.dto.ItemDto;
import com.onurbcd.cli.enums.CurrencyType;
import com.onurbcd.cli.enums.SourceType;
import com.onurbcd.cli.persistency.entity.QSource;
import com.querydsl.core.BooleanBuilder;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SourceRepositoryIT extends AbstractRepositoryIT {

    private static final QSource SOURCE = QSource.source;

    @Autowired
    private SourceRepository repository;

    private UUID incomeSourceId;

    private UUID source(String name, SourceType type, String balance) {
        if (incomeSourceId == null) {
            incomeSourceId = fixtures.incomeSource("Salary", true);
        }

        return fixtures.source(incomeSourceId, name, type, new BigDecimal(balance));
    }

    @Nested
    @DisplayName("deleteUsingId")
    class DeleteUsingId {

        @Test
        void givenExistingId_whenDelete_shouldRemoveOnlyThatRow() {
            var target = source("Wallet", SourceType.USABLE, "10");
            var other = source("Bank", SourceType.USABLE, "20");

            var deleted = repository.deleteUsingId(target);

            assertThat(deleted).isEqualTo(1);
            assertThat(jdbc.queryForList("select id from source", UUID.class)).containsExactly(other);
        }

        @Test
        void givenUnknownId_whenDelete_shouldReturnZero() {
            source("Wallet", SourceType.USABLE, "10");

            assertThat(repository.deleteUsingId(UUID.randomUUID())).isZero();
            assertThat(jdbc.queryForObject("select count(*) from source", Long.class)).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("updateActive")
    class UpdateActive {

        @Test
        void givenExistingId_whenUpdateActive_shouldChangeOnlyThatRow() {
            var target = source("Wallet", SourceType.USABLE, "10");
            var other = source("Bank", SourceType.USABLE, "20");

            var updated = repository.updateActive(target, false);

            assertThat(updated).isEqualTo(1);
            assertThat(active(target)).isFalse();
            assertThat(active(other)).isTrue();
        }

        @Test
        void givenUnknownId_whenUpdateActive_shouldReturnZero() {
            var existing = source("Wallet", SourceType.USABLE, "10");

            assertThat(repository.updateActive(UUID.randomUUID(), false)).isZero();
            assertThat(active(existing)).isTrue();
        }

        private Boolean active(UUID id) {
            return jdbc.queryForObject("select active from source where id = ?", Boolean.class, id);
        }
    }

    @Nested
    @DisplayName("getBalanceSum")
    class GetBalanceSum {

        @Test
        void givenSourceType_whenGetBalanceSum_shouldSumOnlyThatType() {
            source("Wallet", SourceType.USABLE, "10.5");
            source("Bank", SourceType.USABLE, "20.25");
            source("Savings", SourceType.UNUSABLE, "100");

            assertThat(repository.getBalanceSum(SourceType.USABLE)).isEqualByComparingTo("30.75");
            assertThat(repository.getBalanceSum(SourceType.UNUSABLE)).isEqualByComparingTo("100");
        }

        @Test
        void givenNoSourceOfType_whenGetBalanceSum_shouldReturnNull() {
            source("Savings", SourceType.UNUSABLE, "100");

            assertThat(repository.getBalanceSum(SourceType.USABLE)).isNull();
        }

        @Test
        void givenSources_whenGetBalanceSumWithoutType_shouldSumAll() {
            source("Wallet", SourceType.USABLE, "10.5");
            source("Savings", SourceType.UNUSABLE, "-100");

            assertThat(repository.getBalanceSum()).isEqualByComparingTo("-89.5");
        }

        @Test
        void givenNoSources_whenGetBalanceSumWithoutType_shouldReturnNull() {
            assertThat(repository.getBalanceSum()).isNull();
        }
    }

    @Nested
    @DisplayName("updateBalance / getBalance")
    class Balance {

        @Test
        void givenSource_whenUpdateBalance_shouldChangeOnlyThatBalance() {
            var target = source("Wallet", SourceType.USABLE, "10");
            var other = source("Bank", SourceType.USABLE, "20");

            repository.updateBalance(target, new BigDecimal("55.1234"));
            clear();

            assertThat(repository.getBalance(target)).isEqualByComparingTo("55.1234");
            assertThat(repository.getBalance(other)).isEqualByComparingTo("20");
        }

        @Test
        void givenUnknownId_whenGetBalance_shouldReturnNull() {
            assertThat(repository.getBalance(UUID.randomUUID())).isNull();
        }
    }

    @Nested
    @DisplayName("getItems")
    class GetItems {

        @Test
        void givenNullId_whenGetItems_shouldReturnAllOrderedByName() {
            var wallet = source("Wallet", SourceType.USABLE, "10");
            var bank = source("Bank", SourceType.USABLE, "20");

            assertThat(repository.getItems(null)).usingRecursiveFieldByFieldElementComparator()
                    .containsExactly(new ItemDto(bank, "Bank"), new ItemDto(wallet, "Wallet"));
        }

        @Test
        void givenId_whenGetItems_shouldExcludeIt() {
            var wallet = source("Wallet", SourceType.USABLE, "10");
            var bank = source("Bank", SourceType.USABLE, "20");

            assertThat(repository.getItems(wallet)).usingRecursiveFieldByFieldElementComparator()
                    .containsExactly(new ItemDto(bank, "Bank"));
        }
    }

    @Nested
    @DisplayName("custom queries (SourceRepositoryImpl)")
    class Custom {

        @Test
        void givenId_whenGetSingle_shouldProjectTheRowWithItsIncomeSource() {
            var wallet = source("Wallet", SourceType.UNUSABLE, "10.5");
            source("Bank", SourceType.USABLE, "20");

            var result = repository.getSingle(wallet);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(wallet);
            assertThat(result.getName()).isEqualTo("Wallet");
            assertThat(result.getActive()).isTrue();
            assertThat(result.getIncomeSourceId()).isEqualTo(incomeSourceId);
            assertThat(result.getIncomeSourceName()).isEqualTo("Salary");
            assertThat(result.getSourceType()).isEqualTo(SourceType.UNUSABLE);
            assertThat(result.getCurrencyType()).isEqualTo(CurrencyType.DIGITAL);
            assertThat(result.getBalance()).isEqualByComparingTo("10.5");
            assertThat(result.getCreatedDate()).isEqualTo(RepositoryFixtures.CREATED);
            assertThat(result.getLastModifiedDate()).isEqualTo(RepositoryFixtures.CREATED);
        }

        @Test
        void givenUnknownId_whenGetSingle_shouldReturnNull() {
            source("Wallet", SourceType.USABLE, "10");

            assertThat(repository.getSingle(UUID.randomUUID())).isNull();
        }

        @Test
        void givenPredicate_whenGetAll_shouldReturnOnlyMatchingRows() {
            source("Wallet", SourceType.USABLE, "10");
            source("Savings", SourceType.UNUSABLE, "100");

            var result = repository.getAll(SOURCE.sourceType.eq(SourceType.UNUSABLE));

            assertThat(result).extracting("name", "incomeSourceName").containsExactly(
                    Tuple.tuple("Savings", "Salary"));
        }

        @Test
        void givenPageable_whenGetAll_shouldPageSortAndCount() {
            source("Alpha", SourceType.USABLE, "1");
            source("Bravo", SourceType.USABLE, "2");
            source("Charlie", SourceType.USABLE, "3");

            var first = repository.getAll(new BooleanBuilder(), PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "name")));
            var second = repository.getAll(new BooleanBuilder(), PageRequest.of(1, 2, Sort.by(Sort.Direction.DESC, "name")));

            assertThat(first.getTotalElements()).isEqualTo(3);
            assertThat(first.getContent()).extracting("name").containsExactly("Charlie", "Bravo");
            assertThat(second.getTotalElements()).isEqualTo(3);
            assertThat(second.getContent()).extracting("name").containsExactly("Alpha");
        }

        @Test
        void givenPredicate_whenGetSum_shouldSumTheMatchingBalances() {
            source("Wallet", SourceType.USABLE, "10.5");
            source("Bank", SourceType.USABLE, "20.25");
            source("Savings", SourceType.UNUSABLE, "100");

            assertThat(repository.getSum(SOURCE.sourceType.eq(SourceType.USABLE))).isEqualByComparingTo("30.75");
        }

        @Test
        void givenNoMatch_whenGetSum_shouldReturnNull() {
            assertThat(repository.getSum(SOURCE.sourceType.eq(SourceType.USABLE))).isNull();
        }
    }
}
