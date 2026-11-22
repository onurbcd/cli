package com.onurbcd.cli.validator;

import com.onurbcd.cli.dto.category.CategoryDto;
import com.onurbcd.cli.dto.category.CategorySaveDto;
import com.onurbcd.cli.enums.ErrorCode;
import com.onurbcd.cli.exception.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.onurbcd.cli.TestConstants.CATEGORY_ID;
import static com.onurbcd.cli.TestConstants.ID;
import static com.onurbcd.cli.TestConstants.PARENT_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CategoryValidatorTest {

    private static final UUID OTHER_PARENT_ID = UUID.fromString("88888888-8888-8888-8888-888888888888");

    private final CategoryValidator validator = new CategoryValidator();

    private static CategorySaveDto saveDto(UUID parentId) {
        return CategorySaveDto.builder().parentId(parentId).build();
    }

    private static CategoryDto category(int level, boolean lastBranch) {
        var dto = new CategoryDto();
        dto.setId(CATEGORY_ID);
        dto.setParentId(PARENT_ID);
        dto.setLevel((short) level);
        dto.setLastBranch(lastBranch);
        return dto;
    }

    private static void assertError(Throwable thrown, ErrorCode expected) {
        assertThat(thrown).isInstanceOfSatisfying(ApiException.class,
                e -> assertThat(e.getError()).isEqualTo(expected));
    }

    @Nested
    @DisplayName("validate")
    class Validate {

        @Test
        @DisplayName("a new category without parent is rejected")
        void givenNewWithoutParent_shouldThrowParentIsNull() {
            var dto = saveDto(null);

            assertThatThrownBy(() -> validator.validate(dto, null, null))
                    .satisfies(e -> assertError(e, ErrorCode.CATEGORY_PARENT_IS_NULL));
        }

        @Test
        @DisplayName("a new category with a parent is accepted")
        void givenNewWithParent_shouldNotThrow() {
            var dto = saveDto(PARENT_ID);

            assertThatCode(() -> validator.validate(dto, null, null)).doesNotThrowAnyException();

            assertThat(dto).usingRecursiveComparison().isEqualTo(saveDto(PARENT_ID));
        }

        @Test
        @DisplayName("a level one category cannot be updated")
        void givenLevelOne_shouldThrowLevelOneUnchangeable() {
            var dto = saveDto(PARENT_ID);
            var current = category(1, true);

            assertThatThrownBy(() -> validator.validate(dto, current, ID))
                    .satisfies(e -> assertError(e, ErrorCode.CATEGORY_LEVEL_ONE_IS_UNCHANGEABLE));
        }

        @Test
        @DisplayName("the parent of an existing category cannot change")
        void givenDifferentParent_shouldThrowParentUnchangeable() {
            var dto = saveDto(OTHER_PARENT_ID);
            var current = category(2, true);

            assertThatThrownBy(() -> validator.validate(dto, current, ID))
                    .satisfies(e -> assertError(e, ErrorCode.CATEGORY_PARENT_IS_UNCHANGEABLE));
        }

        @Test
        @DisplayName("an existing category below level one keeps its parent and is accepted")
        void givenSameParent_shouldNotThrow() {
            var dto = saveDto(PARENT_ID);
            var current = category(2, true);

            assertThatCode(() -> validator.validate(dto, current, ID)).doesNotThrowAnyException();

            assertThat(dto).usingRecursiveComparison().isEqualTo(saveDto(PARENT_ID));
            assertThat(current).usingRecursiveComparison().isEqualTo(category(2, true));
        }

        @Test
        @DisplayName("an update without the current category fails fast")
        void givenIdWithoutCurrent_shouldThrowNullPointer() {
            var dto = saveDto(PARENT_ID);

            assertThatNullPointerException().isThrownBy(() -> validator.validate(dto, null, ID));
        }
    }

    @Nested
    @DisplayName("validateDelete")
    class ValidateDelete {

        @Test
        @DisplayName("a level one category cannot be deleted")
        void givenLevelOne_shouldThrowCannotDeleteLevelOne() {
            var dto = category(1, true);

            assertThatThrownBy(() -> validator.validateDelete(dto))
                    .satisfies(e -> assertError(e, ErrorCode.CATEGORY_CANNOT_DELETE_LEVEL_ONE));
        }

        @Test
        @DisplayName("a category that is not the last branch cannot be deleted")
        void givenNonLastBranch_shouldThrowDeleteNonLastBranch() {
            var dto = category(2, false);

            assertThatThrownBy(() -> validator.validateDelete(dto))
                    .satisfies(e -> assertError(e, ErrorCode.CATEGORY_DELETE_NON_LAST_BRANCH));
        }

        @Test
        @DisplayName("a last-branch category below level one can be deleted")
        void givenLastBranchBelowLevelOne_shouldNotThrow() {
            var dto = category(2, true);

            assertThatCode(() -> validator.validateDelete(dto)).doesNotThrowAnyException();

            assertThat(dto).usingRecursiveComparison().isEqualTo(category(2, true));
        }
    }
}
