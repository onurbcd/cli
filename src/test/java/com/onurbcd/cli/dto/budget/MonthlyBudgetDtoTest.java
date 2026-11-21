package com.onurbcd.cli.dto.budget;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.onurbcd.cli.TestConstants.ID;
import static org.assertj.core.api.Assertions.assertThat;

class MonthlyBudgetDtoTest {

    @Test
    @DisplayName("the record exposes its components")
    void givenValues_shouldExposeComponents() {
        var dto = new MonthlyBudgetDto(ID, "Water", (short) 2);

        assertThat(dto.id()).isEqualTo(ID);
        assertThat(dto.name()).isEqualTo("Water");
        assertThat(dto.sequence()).isEqualTo((short) 2);
    }

    @Nested
    @DisplayName("compoundName")
    class CompoundName {

        @Test
        @DisplayName("should join sequence and name with a dash")
        void givenSequenceAndName_shouldJoin() {
            assertThat(new MonthlyBudgetDto(ID, "Water", (short) 2).compoundName()).isEqualTo("2 - Water");
        }
    }
}
