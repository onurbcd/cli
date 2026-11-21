package com.onurbcd.cli.dto.budget;

import com.onurbcd.cli.dto.PrimePatchDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BudgetPatchDtoTest {

    @Nested
    @DisplayName("of(paid)")
    class OfPaid {

        @Test
        @DisplayName("should carry the paid flag and leave active null")
        void givenPaid_shouldCarryPaidOnly() {
            var result = BudgetPatchDto.of(true);

            assertThat(result).isInstanceOf(PrimePatchDto.class);
            assertThat(result.getPaid()).isTrue();
            assertThat(result.isActive()).isNull();
        }
    }

    @Nested
    @DisplayName("of(active, paid)")
    class OfActiveAndPaid {

        @Test
        @DisplayName("should carry both flags")
        void givenBoth_shouldCarryBoth() {
            var result = BudgetPatchDto.of(false, true);

            assertThat(result.isActive()).isFalse();
            assertThat(result.getPaid()).isTrue();
        }

        @Test
        @DisplayName("should not mix up the flags")
        void givenSwappedValues_shouldNotMixUp() {
            var result = BudgetPatchDto.of(true, false);

            assertThat(result.isActive()).isTrue();
            assertThat(result.getPaid()).isFalse();
        }
    }

    @Test
    @DisplayName("the paid setter replaces the flag")
    void givenSetter_shouldReplacePaid() {
        var dto = BudgetPatchDto.of(true);

        dto.setPaid(false);

        assertThat(dto.getPaid()).isFalse();
    }
}
