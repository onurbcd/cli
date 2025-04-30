package com.onurbcd.eruservice.dto.filter;

import com.onurbcd.eruservice.enums.BalanceType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.UUID;

@SuperBuilder
@Getter
@Setter
public class BalanceFilter extends AbstractFilterable {

    private LocalDate dayCalendarDate;

    private UUID sourceId;

    private UUID categoryId;

    private BalanceType balanceType;

    private Short dayCalendarYear;

    private Short dayCalendarMonth;

    private Short dayCalendarDayInMonth;
}
