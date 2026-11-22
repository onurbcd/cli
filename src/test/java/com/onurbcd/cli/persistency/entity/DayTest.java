package com.onurbcd.cli.persistency.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DayTest {

    private static Day day(Integer id) {
        var day = new Day();
        day.setId(id);
        return day;
    }

    @Nested
    @DisplayName("equals")
    class Equals {

        @Test
        @DisplayName("should be true for the same instance")
        void givenSameInstance_whenEquals_shouldBeTrue() {
            var day = day(20240315);

            assertThat(day.equals(day)).isTrue();
        }

        @Test
        @DisplayName("should be true, in both directions, for the same id")
        void givenSameId_whenEquals_shouldBeTrueBothWays() {
            var first = day(20240315);
            var second = day(20240315);

            assertThat(first.equals(second)).isTrue();
            assertThat(second.equals(first)).isTrue();
        }

        @Test
        @DisplayName("should be false for a different id")
        void givenDifferentId_whenEquals_shouldBeFalse() {
            assertThat(day(20240315).equals(day(20240316))).isFalse();
        }

        @Test
        @DisplayName("should be false for null")
        void givenNull_whenEquals_shouldBeFalse() {
            assertThat(day(20240315).equals(null)).isFalse();
        }

        @Test
        @DisplayName("should be false for a subclass with the same id, in both directions")
        void givenSubclassWithSameId_whenEquals_shouldBeFalse() {
            var day = day(20240315);
            var subclass = new Day(20240315) {
            };

            assertThat(day.equals(subclass)).isFalse();
            assertThat(subclass.equals(day)).isFalse();
        }

        @Test
        @DisplayName("should be false for another type")
        void givenOtherType_whenEquals_shouldBeFalse() {
            Object notADay = 20240315;

            assertThat(day(20240315).equals(notADay)).isFalse();
        }
    }

    @Test
    @DisplayName("a day is always new, so Spring Data never selects before inserting")
    void givenDay_whenIsNew_shouldBeTrue() {
        assertThat(day(20240315).isNew()).isTrue();
        assertThat(new Day().isNew()).isTrue();
    }

    @Test
    @DisplayName("the id constructor and the id accessors expose the id")
    void givenId_whenConstructAndSet_shouldExposeId() {
        var day = new Day(20240101);

        assertThat(day.getId()).isEqualTo(20240101);

        day.setId(20240202);

        assertThat(day.getId()).isEqualTo(20240202);
    }

    @Nested
    @DisplayName("hashCode")
    class HashCode {

        @Test
        @DisplayName("should be the same for equal days and differ for different ids")
        void givenIds_whenHashCode_shouldFollowEquals() {
            assertThat(day(20240315)).hasSameHashCodeAs(day(20240315));
            assertThat(day(20240315)).doesNotHaveSameHashCodeAs(day(20240316));
        }
    }
}
