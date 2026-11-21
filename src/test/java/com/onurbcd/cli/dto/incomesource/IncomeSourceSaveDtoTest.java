package com.onurbcd.cli.dto.incomesource;

import com.onurbcd.cli.TestValidator;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.shell.jline.tui.component.context.ComponentContext;

import static com.onurbcd.cli.TestFactory.createIncomeSourceDto;
import static org.assertj.core.api.Assertions.assertThat;

class IncomeSourceSaveDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = TestValidator.create();
    }

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("without an income source, should normalize the name and default to active")
        void givenNoIncomeSource_shouldMapName() {
            var context = ComponentContext.empty();
            context.put("name", "  Day   Job ");

            var result = IncomeSourceSaveDto.of(context, null);

            assertThat(result).usingRecursiveComparison()
                    .isEqualTo(IncomeSourceSaveDto.builder().name("Day Job").active(true).build());
        }

        @Test
        @DisplayName("with an inactive income source, should keep it inactive")
        void givenInactiveIncomeSource_shouldKeepInactive() {
            var result = IncomeSourceSaveDto.of(ComponentContext.empty(), createIncomeSourceDto());

            assertThat(result).usingRecursiveComparison()
                    .isEqualTo(IncomeSourceSaveDto.builder().active(false).build());
        }
    }

    @Nested
    @DisplayName("validation")
    class Validation {

        @Test
        @DisplayName("a valid name has no violations")
        void givenValidName_shouldHaveNoViolations() {
            var dto = IncomeSourceSaveDto.builder().name("Job").active(true).build();

            assertThat(TestValidator.messages(validator, dto)).isEmpty();
        }

        @Test
        @DisplayName("a missing name is rejected")
        void givenNoName_shouldBeRejected() {
            var dto = IncomeSourceSaveDto.builder().active(true).build();

            assertThat(TestValidator.messages(validator, dto)).containsExactly("Name is required.");
        }
    }
}
