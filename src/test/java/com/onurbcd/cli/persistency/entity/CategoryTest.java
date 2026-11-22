package com.onurbcd.cli.persistency.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.onurbcd.cli.TestConstants.ID;
import static com.onurbcd.cli.TestConstants.PARENT_ID;
import static org.assertj.core.api.Assertions.assertThat;

class CategoryTest {

    @Nested
    @DisplayName("constructors")
    class Constructors {

        @Test
        @DisplayName("the id constructor only sets the id")
        void givenId_whenConstruct_shouldOnlySetId() {
            var category = new Category(ID);

            assertThat(category.getId()).isEqualTo(ID);
            assertThat(category.getName()).isNull();
            assertThat(category.getActive()).isNull();
            assertThat(category.getParent()).isNull();
            assertThat(category.getLevel()).isNull();
            assertThat(category.getLastBranch()).isNull();
            assertThat(category.getDescription()).isNull();
        }

        @Test
        @DisplayName("the all-args constructor keeps every field")
        void givenAllArgs_whenConstruct_shouldKeepEveryField() {
            var parent = new Category(PARENT_ID);

            var category = new Category(parent, (short) 2, true, "description");

            assertThat(category.getParent()).isSameAs(parent);
            assertThat(category.getLevel()).isEqualTo((short) 2);
            assertThat(category.getLastBranch()).isTrue();
            assertThat(category.getDescription()).isEqualTo("description");
        }
    }

    @Test
    @DisplayName("the builder fills the category and the inherited fields")
    void givenBuilder_whenBuild_shouldFillEveryField() {
        var parent = new Category(PARENT_ID);

        var category = Category.builder()
                .id(ID)
                .name("food")
                .active(true)
                .parent(parent)
                .level((short) 2)
                .lastBranch(false)
                .description("groceries")
                .build();

        assertThat(category.getId()).isEqualTo(ID);
        assertThat(category.getName()).isEqualTo("food");
        assertThat(category.getActive()).isTrue();
        assertThat(category.getParent()).isSameAs(parent);
        assertThat(category.getLevel()).isEqualTo((short) 2);
        assertThat(category.getLastBranch()).isFalse();
        assertThat(category.getDescription()).isEqualTo("groceries");
    }

    @Test
    @DisplayName("setters store each field")
    void givenValues_whenSet_shouldReturnThemFromGetters() {
        var parent = new Category(PARENT_ID);
        var category = new Category();

        category.setParent(parent);
        category.setLevel((short) 3);
        category.setLastBranch(true);
        category.setDescription("text");

        assertThat(category.getParent()).isSameAs(parent);
        assertThat(category.getLevel()).isEqualTo((short) 3);
        assertThat(category.getLastBranch()).isTrue();
        assertThat(category.getDescription()).isEqualTo("text");
    }

    @Test
    @DisplayName("equality follows the id")
    void givenIds_whenEqualsAndHashCode_shouldFollowId() {
        var other = UUID.fromString("99999999-9999-9999-0000-000000000001");

        assertThat(new Category(ID)).isEqualTo(new Category(ID)).hasSameHashCodeAs(new Category(ID));
        assertThat(new Category(ID)).isNotEqualTo(new Category(other)).doesNotHaveSameHashCodeAs(new Category(other));
    }
}
