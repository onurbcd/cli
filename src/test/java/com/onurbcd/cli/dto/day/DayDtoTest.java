package com.onurbcd.cli.dto.day;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DayDtoTest {

    private static DayDto dto(int year, int month) {
        return new DayDto((short) year, (short) month);
    }

    @Nested
    @DisplayName("equals")
    class Equals {

        @Test
        @DisplayName("should be true for the same instance")
        void givenSameInstance_whenEquals_shouldBeTrue() {
            var dto = dto(2024, 3);

            assertThat(dto.equals(dto)).isTrue();
        }

        @Test
        @DisplayName("should be true, in both directions, for the same year and month")
        void givenSameYearAndMonth_whenEquals_shouldBeTrueBothWays() {
            var first = dto(2024, 3);
            var second = dto(2024, 3);

            assertThat(first.equals(second)).isTrue();
            assertThat(second.equals(first)).isTrue();
        }

        @Test
        @DisplayName("should be false when the year or the month differs")
        void givenDifferentYearOrMonth_whenEquals_shouldBeFalse() {
            assertThat(dto(2024, 3).equals(dto(2025, 3))).isFalse();
            assertThat(dto(2024, 3).equals(dto(2024, 4))).isFalse();
        }

        @Test
        @DisplayName("should be false for null and for another type")
        void givenNullOrOtherType_whenEquals_shouldBeFalse() {
            Object notADto = "2024-03";

            assertThat(dto(2024, 3).equals(null)).isFalse();
            assertThat(dto(2024, 3).equals(notADto)).isFalse();
        }
    }

    @Nested
    @DisplayName("hashCode")
    class HashCode {

        @Test
        @DisplayName("should be the same for equal dtos and differ otherwise")
        void givenDtos_whenHashCode_shouldFollowEquals() {
            assertThat(dto(2024, 3).hashCode()).isEqualTo(dto(2024, 3).hashCode());
            assertThat(dto(2024, 3).hashCode()).isNotEqualTo(dto(2024, 4).hashCode());
        }
    }
}
