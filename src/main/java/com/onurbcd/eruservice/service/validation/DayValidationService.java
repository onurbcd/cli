package com.onurbcd.eruservice.service.validation;

import com.onurbcd.eruservice.dto.day.CreateMonthDto;
import com.onurbcd.eruservice.enums.Error;
import com.onurbcd.eruservice.persistency.repository.DayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DayValidationService {

    private final DayRepository repository;

    public void validate(CreateMonthDto dto) {
        var numberOfDaysInMonth = repository.numberOfDaysInMonth(dto);

        Action.checkIf(numberOfDaysInMonth < 1).orElseThrow(Error.MONTH_ALREADY_EXISTS, dto.getCalendarMonth(),
                dto.getCalendarYear());
    }
}
