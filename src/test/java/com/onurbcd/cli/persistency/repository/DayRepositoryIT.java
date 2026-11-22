package com.onurbcd.cli.persistency.repository;

import com.onurbcd.cli.dto.day.CreateMonthDto;
import com.onurbcd.cli.dto.day.DayDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DayRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    private DayRepository repository;

    @Test
    void givenDaysInSeveralMonths_whenNumberOfDaysInMonth_shouldCountOnlyThatMonth() {
        fixtures.day(LocalDate.of(2025, 2, 1));
        fixtures.day(LocalDate.of(2025, 2, 2));
        fixtures.day(LocalDate.of(2025, 2, 3));
        fixtures.day(LocalDate.of(2025, 3, 1));
        fixtures.day(LocalDate.of(2024, 2, 1));

        var result = repository.numberOfDaysInMonth(CreateMonthDto.of((short) 2025, (short) 2));

        assertThat(result).isEqualTo(3);
    }

    @Test
    void givenNoDaysInMonth_whenNumberOfDaysInMonth_shouldReturnZero() {
        fixtures.day(LocalDate.of(2025, 3, 1));

        var result = repository.numberOfDaysInMonth(CreateMonthDto.of((short) 2025, (short) 2));

        assertThat(result).isZero();
    }

    @Test
    void givenRepeatedMonths_whenGetYearsAndMonths_shouldReturnDistinctPairsOrderedByYearThenMonth() {
        fixtures.day(LocalDate.of(2025, 3, 10));
        fixtures.day(LocalDate.of(2025, 3, 11));
        fixtures.day(LocalDate.of(2024, 12, 1));
        fixtures.day(LocalDate.of(2025, 1, 5));

        var result = repository.getYearsAndMonths();

        assertThat(result).isInstanceOf(Set.class);
        assertThat(List.copyOf(result)).containsExactly(dayDto(2024, 12), dayDto(2025, 1), dayDto(2025, 3));
    }

    private static DayDto dayDto(int year, int month) {
        return new DayDto((short) year, (short) month);
    }
}
