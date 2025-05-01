package com.onurbcd.eruservice.service.validation.impl;

import com.onurbcd.eruservice.dto.day.CreateMonthDto;
import com.onurbcd.eruservice.persistency.repository.DayRepository;
import com.onurbcd.eruservice.enums.Error;
import com.onurbcd.eruservice.service.validation.Action;
import com.onurbcd.eruservice.service.validation.DayValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DayValidationServiceImpl implements DayValidationService {

    private final DayRepository repository;

    @Override
    public void validate(CreateMonthDto dto) {
        var numberOfDaysInMonth = repository.numberOfDaysInMonth(dto);

        Action.checkIf(numberOfDaysInMonth < 1).orElseThrow(Error.MONTH_ALREADY_EXISTS, dto.getCalendarMonth(),
                dto.getCalendarYear());
    }
}
