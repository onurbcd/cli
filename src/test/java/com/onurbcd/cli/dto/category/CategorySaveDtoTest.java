package com.onurbcd.cli.dto.category;

import com.onurbcd.cli.TestValidator;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.shell.jline.tui.component.context.ComponentContext;

import static com.onurbcd.cli.TestConstants.PARENT_ID;
import static com.onurbcd.cli.TestFactory.createCategoryDto;
import static org.assertj.core.api.Assertions.assertThat;

class CategorySaveDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = TestValidator.create();
    }

    private static CategorySaveDto validDto() {
        return CategorySaveDto.builder()
                .name("Food")
                .active(true)
                .parentId(PARENT_ID)
                .description("description")
                .build();
    }

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("without a category, should map the context and default to active")
        void givenNoCategory_shouldMapContext() {
            var context = ComponentContext.empty();
            context.put("name", "  Fast   Food ");
            context.put("parentId", PARENT_ID.toString());
            context.put("description", "  some   text ");

            var result = CategorySaveDto.of(context, null);

            assertThat(result).usingRecursiveComparison().isEqualTo(
                    CategorySaveDto.builder()
                            .name("Fast Food")
                            .active(true)
                            .parentId(PARENT_ID)
                            .description("some text")
                            .build());
        }

        @Test
        @DisplayName("with an inactive category, should keep it inactive")
        void givenInactiveCategory_shouldKeepInactive() {
            var result = CategorySaveDto.of(ComponentContext.empty(), createCategoryDto());

            assertThat(result.isActive()).isFalse();
        }

        @Test
        @DisplayName("with an empty context, should leave text fields and parent null")
        void givenEmptyContext_shouldLeaveNulls() {
            var result = CategorySaveDto.of(ComponentContext.empty(), null);

            assertThat(result).usingRecursiveComparison()
                    .isEqualTo(CategorySaveDto.builder().active(true).build());
        }
    }

    @Nested
    @DisplayName("validation")
    class Validation {

        @Test
        @DisplayName("a valid dto has no violations")
        void givenValid_shouldHaveNoViolations() {
            assertThat(TestValidator.messages(validator, validDto())).isEmpty();
        }

        @Test
        @DisplayName("a missing parent is rejected")
        void givenNoParent_shouldBeRejected() {
            var dto = validDto();
            dto.setParentId(null);

            assertThat(TestValidator.messages(validator, dto)).containsExactly("Parent id is required.");
        }

        @Test
        @DisplayName("description may be null, 250 characters is accepted and 251 is rejected")
        void givenDescription_shouldCheckSize() {
            var dto = validDto();
            dto.setDescription(null);
            assertThat(TestValidator.messages(validator, dto)).isEmpty();

            dto.setDescription("d".repeat(250));
            assertThat(TestValidator.messages(validator, dto)).isEmpty();

            dto.setDescription("d".repeat(251));
            assertThat(TestValidator.messages(validator, dto))
                    .containsExactly("Description must be less than 250 characters.");
        }
    }
}
