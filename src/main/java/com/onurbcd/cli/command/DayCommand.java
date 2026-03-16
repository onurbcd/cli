package com.onurbcd.cli.command;

import com.onurbcd.cli.annotation.MaxYear;
import com.onurbcd.cli.annotation.MinYear;
import com.onurbcd.cli.dto.day.CreateMonthDto;
import com.onurbcd.cli.enums.EruTable;
import com.onurbcd.cli.helper.ShellHelper;
import com.onurbcd.cli.service.DayService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import static com.onurbcd.cli.util.Constant.DAY;
import static com.onurbcd.cli.util.Constant.DAY_CREATE_SUCCESS;

@ShellComponent
@ShellCommandGroup(DAY)
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
        var createMonthDto = CreateMonthDto.of(calendarYear, calendarMonth);
        service.createMonth(createMonthDto);
        return shellHelper.success(DAY_CREATE_SUCCESS.formatted(calendarMonth, calendarYear));
    }

    @ShellMethod(key = "day-get", value = "Get list of months.")
    public String get() {
        return shellHelper.printTable(service.getYearsAndMonths(), EruTable.DAY);
    }
}
