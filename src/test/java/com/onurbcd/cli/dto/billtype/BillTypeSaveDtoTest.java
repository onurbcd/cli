package com.onurbcd.cli.dto.billtype;

import com.onurbcd.cli.TestValidator;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.shell.jline.tui.component.context.ComponentContext;

import static com.onurbcd.cli.TestConstants.CATEGORY_ID;
import static com.onurbcd.cli.TestFactory.createBillTypeDto;
import static org.assertj.core.api.Assertions.assertThat;

class BillTypeSaveDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = TestValidator.create();
    }

    private static BillTypeSaveDto validDto() {
        return BillTypeSaveDto.builder()
                .name("Water")
                .active(true)
                .path("bills/water")
                .categoryId(CATEGORY_ID)
                .build();
    }

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("without a bill type, should map the context and default to active")
        void givenNoBillType_shouldMapContext() {
            var context = ComponentContext.empty();
            context.put("name", "  Water   Bill ");
            context.put("path", " bills/water ");
            context.put("categoryId", CATEGORY_ID.toString());

            var result = BillTypeSaveDto.of(context, null);

            assertThat(result).usingRecursiveComparison().isEqualTo(
                    BillTypeSaveDto.builder()
                            .name("Water Bill")
                            .active(true)
                            .path("bills/water")
                            .categoryId(CATEGORY_ID)
                            .build());
        }

        @Test
        @DisplayName("with an inactive bill type, should keep it inactive")
        void givenInactiveBillType_shouldKeepInactive() {
            var result = BillTypeSaveDto.of(ComponentContext.empty(), createBillTypeDto());

            assertThat(result.isActive()).isFalse();
        }

        @Test
        @DisplayName("with an empty context, should leave text fields and category null")
        void givenEmptyContext_shouldLeaveNulls() {
            var result = BillTypeSaveDto.of(ComponentContext.empty(), null);

            assertThat(result).usingRecursiveComparison()
                    .isEqualTo(BillTypeSaveDto.builder().active(true).build());
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
        @DisplayName("a missing category is rejected")
        void givenNoCategory_shouldBeRejected() {
            var dto = validDto();
            dto.setCategoryId(null);

            assertThat(TestValidator.messages(validator, dto)).containsExactly("Category is required");
        }

        @Test
        @DisplayName("a missing path is rejected")
        void givenNoPath_shouldBeRejected() {
            var dto = validDto();
            dto.setPath(null);

            assertThat(TestValidator.messages(validator, dto)).containsExactly("Path is required");
        }

        @ParameterizedTest
        @ValueSource(strings = {"ab", "a/", "/ab"})
        @DisplayName("paths with an invalid size or format are rejected")
        void givenInvalidPath_shouldBeRejected(String path) {
            var dto = validDto();
            dto.setPath(path);

            assertThat(TestValidator.messages(validator, dto)).isNotEmpty();
        }

        @Test
        @DisplayName("a path shorter than 3 characters reports the size message")
        void givenShortPath_shouldReportSize() {
            var dto = validDto();
            dto.setPath("ab");

            assertThat(TestValidator.messages(validator, dto)).containsExactly("Path must be between 3 and 250 characters");
        }

        @Test
        @DisplayName("a path with invalid characters reports the format message")
        void givenInvalidFormat_shouldReportFormat() {
            var dto = validDto();
            dto.setPath("bills water");

            assertThat(TestValidator.messages(validator, dto)).containsExactly("Path format is invalid");
        }

        @Test
        @DisplayName("a path of 250 characters is accepted, 251 is rejected")
        void givenPathLength_shouldCheckMax() {
            var dto = validDto();
            dto.setPath("a".repeat(250));
            assertThat(TestValidator.messages(validator, dto)).isEmpty();

            dto.setPath("a".repeat(251));
            assertThat(TestValidator.messages(validator, dto)).containsExactly("Path must be between 3 and 250 characters");
        }

        @Test
        @DisplayName("a path of exactly 3 characters is accepted")
        void givenMinPath_shouldBeAccepted() {
            var dto = validDto();
            dto.setPath("abc");

            assertThat(TestValidator.messages(validator, dto)).isEmpty();
        }
    }
}
