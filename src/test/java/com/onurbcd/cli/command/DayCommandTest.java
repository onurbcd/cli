package com.onurbcd.cli.command;

import com.onurbcd.cli.dto.day.CreateMonthDto;
import com.onurbcd.cli.dto.day.DayDto;
import com.onurbcd.cli.enums.EruTable;
import com.onurbcd.cli.helper.ShellHelper;
import com.onurbcd.cli.service.DayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DayCommandTest {

    @Mock
    private DayService dayService;
    @Mock
    private ShellHelper shellHelper;
    @Captor
    private ArgumentCaptor<CreateMonthDto> dtoCaptor;

    private DayCommand command;

    @BeforeEach
    void setUp() {
        command = new DayCommand(dayService, shellHelper);
    }

    @Nested
    @DisplayName("create")
    class Create {

        static Stream<Arguments> yearsAndMonths() {
            return Stream.of(
                    Arguments.of("single digit month is zero padded", (short) 2026, (short) 3,
                            "Month 03/2026 created with success."),
                    Arguments.of("double digit month is kept as is", (short) 2030, (short) 12,
                            "Month 12/2030 created with success."));
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("yearsAndMonths")
        @DisplayName("should create the month through the service and return the success message")
        void givenYearAndMonth_whenCreate_shouldCreateMonthAndReturnMessage(String label, Short year, Short month,
                                                                            String expectedMessage) {
            when(shellHelper.success(expectedMessage)).thenReturn("created");

            var result = command.create(year, month);

            assertThat(result).isEqualTo("created");
            verify(dayService).createMonth(dtoCaptor.capture());
            assertThat(dtoCaptor.getValue()).usingRecursiveComparison().isEqualTo(
                    CreateMonthDto.builder().calendarYear(year).calendarMonth(month).build());
            verify(shellHelper).success(expectedMessage);
            verifyNoMoreInteractions(dayService, shellHelper);
        }

        @Test
        @DisplayName("when the service fails, should propagate the error and print no success message")
        void givenServiceFailure_whenCreate_shouldPropagateAndNotPrintSuccess() {
            var failure = new IllegalStateException("boom");
            doThrow(failure).when(dayService).createMonth(any(CreateMonthDto.class));

            assertThatThrownBy(() -> command.create((short) 2026, (short) 3)).isSameAs(failure);

            verify(dayService).createMonth(any(CreateMonthDto.class));
            verifyNoMoreInteractions(dayService);
            verifyNoInteractions(shellHelper);
        }
    }

    @Nested
    @DisplayName("get")
    class Get {

        @Test
        @DisplayName("should print the years and months as a day table")
        void whenGet_shouldPrintYearsAndMonthsTable() {
            Set<DayDto> yearsAndMonths = Set.of(new DayDto((short) 2026, (short) 3));
            when(dayService.getYearsAndMonths()).thenReturn(yearsAndMonths);
            when(shellHelper.printTable(yearsAndMonths, EruTable.DAY)).thenReturn("table");

            var result = command.get();

            assertThat(result).isEqualTo("table");
            verify(dayService).getYearsAndMonths();
            verify(shellHelper).printTable(yearsAndMonths, EruTable.DAY);
            verifyNoMoreInteractions(dayService, shellHelper);
        }
    }
}
