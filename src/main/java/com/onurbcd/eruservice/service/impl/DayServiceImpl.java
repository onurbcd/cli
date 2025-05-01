package com.onurbcd.eruservice.service.impl;

import com.onurbcd.eruservice.dto.day.CreateMonthDto;
import com.onurbcd.eruservice.dto.day.DayDto;
import com.onurbcd.eruservice.enums.Error;
import com.onurbcd.eruservice.persistency.entity.Day;
import com.onurbcd.eruservice.persistency.repository.DayRepository;
import com.onurbcd.eruservice.service.DayService;
import com.onurbcd.eruservice.service.validation.Action;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.IsoFields;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

@Service
@RequiredArgsConstructor
public class DayServiceImpl implements DayService {

    private final DayRepository repository;

    @Override
    public void createMonth(CreateMonthDto dto) {
        Action.checkIf(repository.numberOfDaysInMonth(dto) < 1)
                .orElseThrow(Error.MONTH_ALREADY_EXISTS, dto.getCalendarMonth(), dto.getCalendarYear());

        var days = createDays(dto);
        repository.saveAll(days);
    }

    @Override
    public Set<DayDto> getYearsAndMonths() {
        return repository.getYearsAndMonths();
    }

    @Override
    public Integer createId(LocalDate calendarDate) {
        var strId = String.format("%d%02d%02d", calendarDate.getYear(), calendarDate.getMonthValue(),
                calendarDate.getDayOfMonth());

        return Integer.valueOf(strId);
    }

    private List<Day> createDays(CreateMonthDto dto) {
        var days = new ArrayList<Day>();
        var currentDay = LocalDate.of(dto.getCalendarYear(), dto.getCalendarMonth(), 1);
        var lastDayOfMonth = currentDay.with(lastDayOfMonth());

        while (!currentDay.isAfter(lastDayOfMonth)) {
            var day = createDay(currentDay);
            days.add(day);
            currentDay = currentDay.plusDays(1);
        }

        return days;
    }

    private Day createDay(LocalDate calendarDate) {
        var day = new Day();
        day.setId(createId(calendarDate));
        day.setCalendarDate(calendarDate);
        day.setCalendarYear((short) calendarDate.getYear());
        day.setCalendarQuarter((short) calendarDate.get(IsoFields.QUARTER_OF_YEAR));
        day.setCalendarMonth((short) calendarDate.getMonthValue());
        day.setCalendarMonthName(calendarDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toLowerCase());
        day.setCalendarWeekInYear((short) calendarDate.get(WeekFields.ISO.weekOfYear()));
        day.setCalendarWeekInMonth((short) calendarDate.get(WeekFields.ISO.weekOfMonth()));
        day.setCalendarDayInYear((short) calendarDate.getDayOfYear());
        day.setCalendarDayInWeek((short) calendarDate.get(WeekFields.ISO.dayOfWeek()));
        day.setCalendarDayInMonth((short) calendarDate.getDayOfMonth());
        day.setCalendarWeekdayName(calendarDate.getDayOfWeek().name().toLowerCase());
        return day;
    }
}
