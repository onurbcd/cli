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
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.CommandGroup;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

import static com.onurbcd.cli.util.Constant.DAY;
import static com.onurbcd.cli.util.Constant.DAY_CREATE_SUCCESS;

@Component
@CommandGroup(name = DAY)
@RequiredArgsConstructor
public class DayCommand {

    private final DayService service;
    private final ShellHelper shellHelper;

    @Command(name = "day-create", description = "Create month.")
    public String create(
            @Option(shortName = 'y', longName = "calendarYear", description = "The calendar's year.", required = true)
            @NotNull
            @MinYear
            @MaxYear
            Short calendarYear,

            @Option(shortName = 'm', longName = "calendarMonth", description = "The calendar's month.", required = true)
            @NotNull
            @Min(1)
            @Max(12)
            Short calendarMonth
    ) {
        var createMonthDto = CreateMonthDto.of(calendarYear, calendarMonth);
        service.createMonth(createMonthDto);
        return shellHelper.success(DAY_CREATE_SUCCESS.formatted(calendarMonth, calendarYear));
    }

    @Command(name = "day-get", description = "Get list of months.")
    public String get() {
        return shellHelper.printTable(service.getYearsAndMonths(), EruTable.DAY);
    }
}
