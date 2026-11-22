package com.onurbcd.cli.dto.budget;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CopyBudgetDtoTest {

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("should build the origin and destination references in the right order")
        void givenFromAndTo_shouldBuildRefs() {
            var dto = CopyBudgetDto.of((short) 2024, (short) 1, (short) 2025, (short) 2);

            assertThat(dto.getFrom()).usingRecursiveComparison().isEqualTo(RefDto.of((short) 2024, (short) 1));
            assertThat(dto.getTo()).usingRecursiveComparison().isEqualTo(RefDto.of((short) 2025, (short) 2));
        }
    }

    @Test
    @DisplayName("setters replace the references")
    void givenSetters_shouldReplaceRefs() {
        var dto = CopyBudgetDto.of((short) 2024, (short) 1, (short) 2025, (short) 2);
        var from = RefDto.of((short) 2000, (short) 12);
        var to = RefDto.of((short) 2001, (short) 11);

        dto.setFrom(from);
        dto.setTo(to);

        assertThat(dto.getFrom()).isSameAs(from);
        assertThat(dto.getTo()).isSameAs(to);
    }
}
