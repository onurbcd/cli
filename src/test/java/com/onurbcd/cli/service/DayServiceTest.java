package com.onurbcd.cli.service;

import com.onurbcd.cli.dto.day.CreateMonthDto;
import com.onurbcd.cli.dto.day.DayDto;
import com.onurbcd.cli.enums.ErrorCode;
import com.onurbcd.cli.exception.ApiException;
import com.onurbcd.cli.persistency.entity.Day;
import com.onurbcd.cli.persistency.repository.DayRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DayServiceTest {

    @Mock
    private DayRepository repository;
    @Captor
    private ArgumentCaptor<List<Day>> daysCaptor;
    @InjectMocks
    private DayService service;

    private static Day day(int id, LocalDate date, int quarter, int month, String monthName, int weekInYear,
                           int weekInMonth, int dayInYear, int dayInWeek, int dayInMonth, String weekday) {
        var day = new Day();
        day.setId(id);
        day.setCalendarDate(date);
        day.setCalendarYear((short) date.getYear());
        day.setCalendarQuarter((short) quarter);
        day.setCalendarMonth((short) month);
        day.setCalendarMonthName(monthName);
        day.setCalendarWeekInYear((short) weekInYear);
        day.setCalendarWeekInMonth((short) weekInMonth);
        day.setCalendarDayInYear((short) dayInYear);
        day.setCalendarDayInWeek((short) dayInWeek);
        day.setCalendarDayInMonth((short) dayInMonth);
        day.setCalendarWeekdayName(weekday);
        return day;
    }

    @Nested
    @DisplayName("createMonth")
    class CreateMonth {

        @ParameterizedTest(name = "[{index}] {1}/{0} -> {2} days")
        @CsvSource({"2024, 1, 31", "2024, 2, 29", "2023, 2, 28", "2024, 4, 30", "2024, 12, 31"})
        @DisplayName("when the month does not exist, should save one day per calendar day, in order")
        void givenNewMonth_shouldSaveEveryDay(short year, short month, int expectedDays) {
            var dto = CreateMonthDto.of(year, month);
            when(repository.numberOfDaysInMonth(dto)).thenReturn(0L);

            service.createMonth(dto);

            verify(repository).numberOfDaysInMonth(dto);
            verify(repository).saveAll(daysCaptor.capture());
            var days = daysCaptor.getValue();
            assertThat(days).hasSize(expectedDays);
            for (var index = 0; index < expectedDays; index++) {
                var date = LocalDate.of(year, month, index + 1);
                assertThat(days.get(index).getCalendarDate()).isEqualTo(date);
                assertThat(days.get(index).getId()).isEqualTo(service.createId(date));
                assertThat(days.get(index).getCalendarDayInMonth()).isEqualTo((short) (index + 1));
            }
            verifyNoMoreInteractions(repository);
        }

        @Test
        @DisplayName("should fill every calendar attribute of the first and last day of the month")
        void givenMarch2024_shouldFillAllAttributes() {
            var dto = CreateMonthDto.of((short) 2024, (short) 3);

            service.createMonth(dto);

            verify(repository).numberOfDaysInMonth(dto);
            verify(repository).saveAll(daysCaptor.capture());
            var days = daysCaptor.getValue();
            assertThat(days.getFirst()).usingRecursiveComparison().isEqualTo(
                    day(20240301, LocalDate.of(2024, 3, 1), 1, 3, "march", 9, 0, 61, 5, 1, "friday"));
            assertThat(days.getLast()).usingRecursiveComparison().isEqualTo(
                    day(20240331, LocalDate.of(2024, 3, 31), 1, 3, "march", 13, 4, 91, 7, 31, "sunday"));
            verifyNoMoreInteractions(repository);
        }

        @ParameterizedTest(name = "[{index}] month {0} -> quarter {1}, {2}")
        @CsvSource({"1, 1, january", "2, 1, february", "3, 1, march", "4, 2, april", "5, 2, may", "6, 2, june",
                "7, 3, july", "8, 3, august", "9, 3, september", "10, 4, october", "11, 4, november",
                "12, 4, december"})
        @DisplayName("should set the quarter and the lower case English month name")
        void givenMonth_shouldSetQuarterAndMonthName(short month, short quarter, String monthName) {
            service.createMonth(CreateMonthDto.of((short) 2024, month));

            verify(repository).numberOfDaysInMonth(CreateMonthDto.of((short) 2024, month));
            verify(repository).saveAll(daysCaptor.capture());
            assertThat(daysCaptor.getValue()).allSatisfy(day -> {
                assertThat(day.getCalendarMonth()).isEqualTo(month);
                assertThat(day.getCalendarQuarter()).isEqualTo(quarter);
                assertThat(day.getCalendarMonthName()).isEqualTo(monthName);
                assertThat(day.getCalendarYear()).isEqualTo((short) 2024);
            });
            verifyNoMoreInteractions(repository);
        }

        @ParameterizedTest(name = "[{index}] {0} days already exist")
        @CsvSource({"1", "28", "31"})
        @DisplayName("when the month already exists, should fail with month already exists and save nothing")
        void givenExistingMonth_shouldThrow(long existingDays) {
            var dto = CreateMonthDto.of((short) 2024, (short) 3);
            when(repository.numberOfDaysInMonth(dto)).thenReturn(existingDays);

            assertThatThrownBy(() -> service.createMonth(dto))
                    .isInstanceOfSatisfying(ApiException.class, e -> {
                        assertThat(e.getError()).isEqualTo(ErrorCode.MONTH_ALREADY_EXISTS);
                        assertThat(e.getMessage()).isEqualTo("Month 03/2024 already exists");
                    });

            verify(repository).numberOfDaysInMonth(dto);
            verifyNoMoreInteractions(repository);
        }
    }

    @Nested
    @DisplayName("getYearsAndMonths")
    class GetYearsAndMonths {

        @Test
        @DisplayName("should return what the repository returns")
        void shouldReturnRepositoryResult() {
            var months = Set.of(new DayDto((short) 2024, (short) 3));
            when(repository.getYearsAndMonths()).thenReturn(months);

            var result = service.getYearsAndMonths();

            assertThat(result).isSameAs(months);
            verify(repository).getYearsAndMonths();
            verifyNoMoreInteractions(repository);
        }
    }

    @Nested
    @DisplayName("createId")
    class CreateId {

        @ParameterizedTest(name = "[{index}] {0} -> {1}")
        @CsvSource({"2024-03-05, 20240305", "2024-01-01, 20240101", "2024-12-31, 20241231", "2024-10-10, 20241010"})
        @DisplayName("should join year, two-digit month and two-digit day")
        void givenDate_shouldBuildYyyyMmDd(LocalDate date, int expected) {
            assertThat(service.createId(date)).isEqualTo(expected);
        }
    }
}
