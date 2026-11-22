package com.onurbcd.cli.persistency.repository;

import com.onurbcd.cli.dto.ItemDto;
import com.onurbcd.cli.dto.billtype.BillTypeValuesDto;
import com.onurbcd.cli.persistency.entity.BillType;
import com.onurbcd.cli.persistency.entity.QBillType;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BillTypeRepositoryIT extends AbstractRepositoryIT {

    private static final QBillType BILL_TYPE = QBillType.billType;

    @Autowired
    private BillTypeRepository repository;

    private UUID categoryId;

    private UUID billType(String name, String path) {
        if (categoryId == null) {
            categoryId = fixtures.category("Home", null, 1, true);
        }

        return fixtures.billType(name, categoryId, path);
    }

    @Nested
    @DisplayName("deleteUsingId")
    class DeleteUsingId {

        @Test
        void givenExistingId_whenDelete_shouldRemoveOnlyThatRow() {
            var target = billType("Rent", "home/rent");
            var other = billType("Water", "home/water");

            assertThat(repository.deleteUsingId(target)).isEqualTo(1);
            assertThat(jdbc.queryForList("select id from bill_type", UUID.class)).containsExactly(other);
        }

        @Test
        void givenUnknownId_whenDelete_shouldReturnZero() {
            billType("Rent", "home/rent");

            assertThat(repository.deleteUsingId(UUID.randomUUID())).isZero();
            assertThat(jdbc.queryForObject("select count(*) from bill_type", Long.class)).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("updateActive")
    class UpdateActive {

        @Test
        void givenExistingId_whenUpdateActive_shouldChangeOnlyThatRow() {
            var target = billType("Rent", "home/rent");
            var other = billType("Water", "home/water");

            assertThat(repository.updateActive(target, false)).isEqualTo(1);
            assertThat(active(target)).isFalse();
            assertThat(active(other)).isTrue();
        }

        @Test
        void givenUnknownId_whenUpdateActive_shouldReturnZero() {
            var existing = billType("Rent", "home/rent");

            assertThat(repository.updateActive(UUID.randomUUID(), false)).isZero();
            assertThat(active(existing)).isTrue();
        }

        private Boolean active(UUID id) {
            return jdbc.queryForObject("select active from bill_type where id = ?", Boolean.class, id);
        }
    }

    @Nested
    @DisplayName("get")
    class Get {

        @Test
        void givenId_whenGet_shouldFetchTheCategoryEagerly() {
            var rent = billType("Rent", "home/rent");
            clear();

            var result = repository.get(rent);

            assertThat(result).isPresent();
            BillType billType = result.get();
            assertThat(billType.getId()).isEqualTo(rent);
            assertThat(billType.getName()).isEqualTo("Rent");
            assertThat(billType.getPath()).isEqualTo("home/rent");
            assertThat(Hibernate.isInitialized(billType.getCategory())).isTrue();
            assertThat(billType.getCategory().getId()).isEqualTo(categoryId);
        }

        @Test
        void givenUnknownId_whenGet_shouldReturnEmpty() {
            assertThat(repository.get(UUID.randomUUID())).isEmpty();
        }
    }

    @Nested
    @DisplayName("getValues")
    class GetValues {

        @Test
        void givenId_whenGetValues_shouldProjectPathAndCategory() {
            var rent = billType("Rent", "home/rent");
            billType("Water", "home/water");

            assertThat(repository.getValues(rent)).contains(new BillTypeValuesDto("home/rent", categoryId));
        }

        @Test
        void givenUnknownId_whenGetValues_shouldReturnEmpty() {
            assertThat(repository.getValues(UUID.randomUUID())).isEmpty();
        }
    }

    @Nested
    @DisplayName("getItems")
    class GetItems {

        @Test
        void givenNullId_whenGetItems_shouldReturnAllOrderedByName() {
            var rent = billType("Rent", "home/rent");
            var water = billType("Water", "home/water");

            assertThat(repository.getItems(null)).usingRecursiveFieldByFieldElementComparator()
                    .containsExactly(new ItemDto(rent, "Rent"), new ItemDto(water, "Water"));
        }

        @Test
        void givenId_whenGetItems_shouldExcludeIt() {
            var rent = billType("Rent", "home/rent");
            var water = billType("Water", "home/water");

            assertThat(repository.getItems(rent)).usingRecursiveFieldByFieldElementComparator()
                    .containsExactly(new ItemDto(water, "Water"));
        }
    }

    @Nested
    @DisplayName("custom queries (BillTypeRepositoryImpl)")
    class Custom {

        @Test
        void givenId_whenGetSingle_shouldProjectTheRowWithItsCategory() {
            var rent = billType("Rent", "home/rent");
            billType("Water", "home/water");

            var result = repository.getSingle(rent);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(rent);
            assertThat(result.getName()).isEqualTo("Rent");
            assertThat(result.getActive()).isTrue();
            assertThat(result.getPath()).isEqualTo("home/rent");
            assertThat(result.getCategoryId()).isEqualTo(categoryId);
            assertThat(result.getCategoryName()).isEqualTo("Home");
            assertThat(result.getCreatedDate()).isEqualTo(RepositoryFixtures.CREATED);
        }

        @Test
        void givenUnknownId_whenGetSingle_shouldReturnNull() {
            billType("Rent", "home/rent");

            assertThat(repository.getSingle(UUID.randomUUID())).isNull();
        }

        @Test
        void givenPredicate_whenGetAll_shouldReturnOnlyMatchingRows() {
            billType("Rent", "home/rent");
            billType("Water", "home/water");

            var result = repository.getAll(BILL_TYPE.path.eq("home/water"));

            assertThat(result).extracting("name", "categoryName")
                    .containsExactly(Tuple.tuple("Water", "Home"));
        }

        @Test
        void givenPageable_whenGetAll_shouldPageSortAndCount() {
            billType("Rent", "home/rent");
            billType("Water", "home/water");
            billType("Energy", "home/energy");

            var first = repository.getAll(new BooleanBuilder(), PageRequest.of(0, 2, Sort.by("name")));
            var second = repository.getAll(new BooleanBuilder(), PageRequest.of(1, 2, Sort.by("name")));

            assertThat(first.getTotalElements()).isEqualTo(3);
            assertThat(first.getContent()).extracting("name").containsExactly("Energy", "Rent");
            assertThat(second.getContent()).extracting("name").containsExactly("Water");
        }

        @Test
        void whenGetSum_shouldReturnZero() {
            assertThat(repository.getSum(new BooleanBuilder())).isEqualTo(BigDecimal.ZERO);
        }
    }
}
