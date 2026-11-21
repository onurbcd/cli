package com.onurbcd.cli.dto.source;

import com.onurbcd.cli.TestValidator;
import com.onurbcd.cli.enums.CurrencyType;
import com.onurbcd.cli.enums.SourceType;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.shell.jline.tui.component.context.ComponentContext;

import java.math.BigDecimal;

import static com.onurbcd.cli.TestConstants.ID;
import static com.onurbcd.cli.TestFactory.createSourceDto;
import static org.assertj.core.api.Assertions.assertThat;

class SourceSaveDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = TestValidator.create();
    }

    private static SourceSaveDto validDto() {
        return SourceSaveDto.builder()
                .name("Wallet")
                .active(true)
                .incomeSourceId(ID)
                .sourceType(SourceType.USABLE)
                .currencyType(CurrencyType.MONEY)
                .balance(new BigDecimal("10.00"))
                .build();
    }

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("without a source, should map the context and default to active")
        void givenNoSource_shouldMapContext() {
            var context = ComponentContext.empty();
            context.put("name", "  My   Wallet ");
            context.put("incomeSourceId", ID.toString());
            context.put("sourceType", "UNUSABLE");
            context.put("currencyType", "DIGITAL");
            context.put("balance", "12.50");

            var result = SourceSaveDto.of(context, null);

            assertThat(result).usingRecursiveComparison().isEqualTo(
                    SourceSaveDto.builder()
                            .name("My Wallet")
                            .active(true)
                            .incomeSourceId(ID)
                            .sourceType(SourceType.UNUSABLE)
                            .currencyType(CurrencyType.DIGITAL)
                            .balance(new BigDecimal("12.50"))
                            .build());
        }

        @Test
        @DisplayName("with an inactive source and an empty context, should keep it inactive and leave the rest null")
        void givenInactiveSource_shouldKeepInactive() {
            var result = SourceSaveDto.of(ComponentContext.empty(), createSourceDto());

            assertThat(result).usingRecursiveComparison()
                    .isEqualTo(SourceSaveDto.builder().active(false).build());
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
        @DisplayName("each required field is reported when null")
        void givenNulls_shouldReportEachRequired() {
            var dto = validDto();
            dto.setIncomeSourceId(null);
            dto.setSourceType(null);
            dto.setCurrencyType(null);
            dto.setBalance(null);

            assertThat(TestValidator.messages(validator, dto)).containsExactlyInAnyOrder(
                    "Income source id is required.",
                    "Source type is required.",
                    "Currency type is required.",
                    "Balance is required.");
        }

        @ParameterizedTest
        @ValueSource(strings = {"-999999999999999", "999999999999999"})
        @DisplayName("balance on the limits is accepted")
        void givenBalanceOnLimit_shouldBeAccepted(String balance) {
            var dto = validDto();
            dto.setBalance(new BigDecimal(balance));

            assertThat(TestValidator.messages(validator, dto)).isEmpty();
        }

        @Test
        @DisplayName("balance below the minimum is rejected")
        void givenBalanceBelowMin_shouldBeRejected() {
            var dto = validDto();
            dto.setBalance(new BigDecimal("-1000000000000000"));

            assertThat(TestValidator.messages(validator, dto))
                    .containsExactly("Balance must be greater than or equal to -999999999999999.");
        }

        @Test
        @DisplayName("balance above the maximum is rejected")
        void givenBalanceAboveMax_shouldBeRejected() {
            var dto = validDto();
            dto.setBalance(new BigDecimal("1000000000000000"));

            assertThat(TestValidator.messages(validator, dto))
                    .containsExactly("Balance must be less than or equal to 999999999999999.");
        }
    }
}
