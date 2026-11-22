package com.onurbcd.cli.persistency.repository;

import com.onurbcd.cli.dto.ItemDto;
import com.onurbcd.cli.persistency.entity.IncomeSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class IncomeSourceRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    private IncomeSourceRepository repository;

    @Nested
    @DisplayName("deleteUsingId")
    class DeleteUsingId {

        @Test
        void givenExistingId_whenDelete_shouldRemoveOnlyThatRow() {
            var target = fixtures.incomeSource("Salary", true);
            var other = fixtures.incomeSource("Freelance", true);

            var deleted = repository.deleteUsingId(target);

            assertThat(deleted).isEqualTo(1);
            assertThat(ids()).containsExactly(other);
        }

        @Test
        void givenUnknownId_whenDelete_shouldReturnZeroAndKeepRows() {
            var existing = fixtures.incomeSource("Salary", true);

            var deleted = repository.deleteUsingId(UUID.randomUUID());

            assertThat(deleted).isZero();
            assertThat(ids()).containsExactly(existing);
        }
    }

    @Nested
    @DisplayName("updateActive")
    class UpdateActive {

        @Test
        void givenExistingId_whenUpdateActive_shouldChangeOnlyThatRow() {
            var target = fixtures.incomeSource("Salary", true);
            var other = fixtures.incomeSource("Freelance", true);

            var updated = repository.updateActive(target, false);

            assertThat(updated).isEqualTo(1);
            assertThat(active(target)).isFalse();
            assertThat(active(other)).isTrue();
        }

        @Test
        void givenUnknownId_whenUpdateActive_shouldReturnZero() {
            var existing = fixtures.incomeSource("Salary", true);

            var updated = repository.updateActive(UUID.randomUUID(), false);

            assertThat(updated).isZero();
            assertThat(active(existing)).isTrue();
        }
    }

    @Nested
    @DisplayName("getItems")
    class GetItems {

        @Test
        void givenNullId_whenGetItems_shouldReturnAllOrderedByName() {
            var salary = fixtures.incomeSource("Salary", true);
            var bonus = fixtures.incomeSource("Bonus", true);

            var result = repository.getItems(null);

            assertThat(result).usingRecursiveFieldByFieldElementComparator()
                    .containsExactly(new ItemDto(bonus, "Bonus"), new ItemDto(salary, "Salary"));
        }

        @Test
        void givenId_whenGetItems_shouldExcludeIt() {
            var salary = fixtures.incomeSource("Salary", true);
            var bonus = fixtures.incomeSource("Bonus", true);

            var result = repository.getItems(salary);

            assertThat(result).usingRecursiveFieldByFieldElementComparator()
                    .containsExactly(new ItemDto(bonus, "Bonus"));
        }
    }

    private List<UUID> ids() {
        return jdbc.queryForList("select id from income_source", UUID.class);
    }

    private Boolean active(UUID id) {
        return jdbc.queryForObject("select active from income_source where id = ?", Boolean.class, id);
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("a new entity without id should get a generated UUID and be persisted")
        void givenNewEntity_whenSave_shouldGenerateIdAndPersist() {
            var entity = IncomeSource.builder().name("Salary").active(true).build();

            var saved = repository.save(entity);
            clear();

            assertThat(saved.getId()).isNotNull();
            assertThat(jdbc.queryForObject("select name from income_source where id = ?", String.class, saved.getId()))
                    .isEqualTo("Salary");
        }

        @Test
        @DisplayName("two new entities should get different ids")
        void givenTwoNewEntities_whenSave_shouldGenerateDifferentIds() {
            var first = repository.save(IncomeSource.builder().name("Salary").active(true).build());
            var second = repository.save(IncomeSource.builder().name("Bonus").active(true).build());

            assertThat(first.getId()).isNotEqualTo(second.getId());
        }
    }
}
