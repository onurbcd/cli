package com.onurbcd.eruservice.command;

import com.onurbcd.eruservice.enums.EruTable;
import com.onurbcd.eruservice.helper.ShellHelper;
import com.onurbcd.eruservice.dto.day.CreateMonthDto;
import com.onurbcd.eruservice.service.DayService;
import com.onurbcd.eruservice.validation.constraint.MaxYear;
import com.onurbcd.eruservice.validation.constraint.MinYear;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
@ShellCommandGroup("Day")
@RequiredArgsConstructor
public class DayCommand {

    private final DayService service;
    private final ShellHelper shellHelper;

    @ShellMethod(key = "day-create", value = "Create month.")
    public String create(
            @ShellOption(value = {"calendarYear", "-y"}, help = "The calendar's year.")
            @NotNull
            @MinYear
            @MaxYear
            Short calendarYear,

            @ShellOption(value = {"calendarMonth", "-m"}, help = "The calendar's month.")
            @NotNull
            @Min(1)
            @Max(12)
            Short calendarMonth
    ) {
        service.createMonth(CreateMonthDto.of(calendarYear, calendarMonth));
        return String.format("Month %02d/%d created with success.", calendarMonth, calendarYear);
    }

    @ShellMethod(key = "day-get", value = "Get list of months.")
    public String get() {
        return shellHelper.printTable(service.getYearsAndMonths(), EruTable.DAY);
    }
}
