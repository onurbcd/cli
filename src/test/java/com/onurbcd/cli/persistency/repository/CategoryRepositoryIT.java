package com.onurbcd.cli.persistency.repository;

import com.onurbcd.cli.dto.ItemDto;
import com.onurbcd.cli.persistency.entity.Category;
import com.onurbcd.cli.persistency.entity.QCategory;
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

/**
 * The migration V2 seeds the root category "Atar"; every assertion accounts for it.
 */
class CategoryRepositoryIT extends AbstractRepositoryIT {

    private static final QCategory CATEGORY = QCategory.category;
    private static final String ATAR = "Atar";

    @Autowired
    private CategoryRepository repository;

    private UUID atarId() {
        return jdbc.queryForObject("select id from category where name = ?", UUID.class, ATAR);
    }

    @Nested
    @DisplayName("updateActive")
    class UpdateActive {

        @Test
        void givenExistingId_whenUpdateActive_shouldChangeOnlyThatRow() {
            var food = fixtures.category("Food", atarId(), 2, true);

            var updated = repository.updateActive(food, false);

            assertThat(updated).isEqualTo(1);
            assertThat(active(food)).isFalse();
            assertThat(active(atarId())).isTrue();
        }

        @Test
        void givenUnknownId_whenUpdateActive_shouldReturnZero() {
            assertThat(repository.updateActive(UUID.randomUUID(), false)).isZero();
            assertThat(active(atarId())).isTrue();
        }

        private Boolean active(UUID id) {
            return jdbc.queryForObject("select active from category where id = ?", Boolean.class, id);
        }
    }

    @Nested
    @DisplayName("updateLastBranch")
    class UpdateLastBranch {

        @Test
        void givenCategory_whenUpdateLastBranch_shouldChangeOnlyThatRow() {
            var food = fixtures.category("Food", atarId(), 2, true);
            var home = fixtures.category("Home", atarId(), 2, true);

            repository.updateLastBranch(false, food);

            assertThat(lastBranch(food)).isFalse();
            assertThat(lastBranch(home)).isTrue();
        }

        private Boolean lastBranch(UUID id) {
            return jdbc.queryForObject("select last_branch from category where id = ?", Boolean.class, id);
        }
    }

    @Nested
    @DisplayName("countChildren")
    class CountChildren {

        @Test
        void givenParent_whenCountChildren_shouldCountOnlyDirectChildren() {
            var food = fixtures.category("Food", atarId(), 2, false);
            fixtures.category("Fruit", food, 3, true);
            fixtures.category("Meat", food, 3, true);
            fixtures.category("Home", atarId(), 2, true);

            assertThat(repository.countChildren(food)).isEqualTo(2);
            assertThat(repository.countChildren(atarId())).isEqualTo(2);
        }

        @Test
        void givenLeaf_whenCountChildren_shouldReturnZero() {
            var home = fixtures.category("Home", atarId(), 2, true);

            assertThat(repository.countChildren(home)).isZero();
        }
    }

    @Nested
    @DisplayName("getCategoryItems")
    class GetCategoryItems {

        @Test
        void givenNullIdAndKeepAtar_whenGetCategoryItems_shouldReturnAllOrderedByName() {
            var home = fixtures.category("Home", atarId(), 2, true);
            var bank = fixtures.category("Bank", atarId(), 2, true);

            var result = repository.getCategoryItems(null, false);

            assertThat(result).usingRecursiveFieldByFieldElementComparator().containsExactly(
                    new ItemDto(atarId(), ATAR), new ItemDto(bank, "Bank"), new ItemDto(home, "Home"));
        }

        @Test
        void givenNullIdAndExcludeAtar_whenGetCategoryItems_shouldOmitAtar() {
            var home = fixtures.category("Home", atarId(), 2, true);

            var result = repository.getCategoryItems(null, true);

            assertThat(result).usingRecursiveFieldByFieldElementComparator()
                    .containsExactly(new ItemDto(home, "Home"));
        }

        @Test
        void givenIdAndKeepAtar_whenGetCategoryItems_shouldOmitOnlyThatId() {
            var home = fixtures.category("Home", atarId(), 2, true);
            var bank = fixtures.category("Bank", atarId(), 2, true);

            var result = repository.getCategoryItems(home, false);

            assertThat(result).usingRecursiveFieldByFieldElementComparator()
                    .containsExactly(new ItemDto(atarId(), ATAR), new ItemDto(bank, "Bank"));
        }

        @Test
        void givenIdAndExcludeAtar_whenGetCategoryItems_shouldOmitBoth() {
            var home = fixtures.category("Home", atarId(), 2, true);
            var bank = fixtures.category("Bank", atarId(), 2, true);

            var result = repository.getCategoryItems(home, true);

            assertThat(result).usingRecursiveFieldByFieldElementComparator()
                    .containsExactly(new ItemDto(bank, "Bank"));
        }
    }

    @Nested
    @DisplayName("custom queries (CategoryRepositoryImpl)")
    class Custom {

        @Test
        void givenChild_whenGetSingle_shouldProjectItsParent() {
            var food = fixtures.category("Food", atarId(), 2, true);

            var result = repository.getSingle(food);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(food);
            assertThat(result.getName()).isEqualTo("Food");
            assertThat(result.getActive()).isTrue();
            assertThat(result.getParentId()).isEqualTo(atarId());
            assertThat(result.getParentName()).isEqualTo(ATAR);
            assertThat(result.getLevel()).isEqualTo((short) 2);
            assertThat(result.getLastBranch()).isTrue();
            assertThat(result.getDescription()).isEqualTo("description of Food");
            assertThat(result.getCreatedDate()).isEqualTo(RepositoryFixtures.CREATED);
        }

        @Test
        void givenRootWithoutParent_whenGetSingle_shouldKeepItBecauseTheJoinIsLeft() {
            var result = repository.getSingle(atarId());

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo(ATAR);
            assertThat(result.getParentId()).isNull();
            assertThat(result.getParentName()).isNull();
        }

        @Test
        void givenUnknownId_whenGetSingle_shouldReturnNull() {
            assertThat(repository.getSingle(UUID.randomUUID())).isNull();
        }

        @Test
        void givenPredicate_whenGetAll_shouldReturnOnlyMatchingRows() {
            fixtures.category("Food", atarId(), 2, true);
            fixtures.category("Home", atarId(), 2, true);

            var result = repository.getAll(CATEGORY.name.eq("Home"));

            assertThat(result).extracting("name", "parentName")
                    .containsExactly(Tuple.tuple("Home", ATAR));
        }

        @Test
        void givenPageable_whenGetAll_shouldPageSortAndCount() {
            fixtures.category("Food", atarId(), 2, true);
            fixtures.category("Home", atarId(), 2, true);

            var first = repository.getAll(new BooleanBuilder(), PageRequest.of(0, 2, Sort.by("name")));
            var second = repository.getAll(new BooleanBuilder(), PageRequest.of(1, 2, Sort.by("name")));

            assertThat(first.getTotalElements()).isEqualTo(3);
            assertThat(first.getContent()).extracting("name").containsExactly(ATAR, "Food");
            assertThat(second.getContent()).extracting("name").containsExactly("Home");
        }

        @Test
        void whenGetSum_shouldReturnZero() {
            assertThat(repository.getSum(new BooleanBuilder())).isEqualTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("a new child category should get a generated UUID and keep its parent")
        void givenNewCategory_whenSave_shouldGenerateIdAndPersistWithParent() {
            var category = Category.builder().name("Test").description("Description").active(true)
                    .parent(new Category(atarId())).level((short) 2).lastBranch(true).build();

            var saved = repository.save(category);
            clear();

            assertThat(saved.getId()).isNotNull();
            var row = repository.getSingle(saved.getId());
            assertThat(row).isNotNull();
            assertThat(row.getName()).isEqualTo("Test");
            assertThat(row.getParentId()).isEqualTo(atarId());
            assertThat(row.getLevel()).isEqualTo((short) 2);
            assertThat(row.getDescription()).isEqualTo("Description");
        }
    }
}
