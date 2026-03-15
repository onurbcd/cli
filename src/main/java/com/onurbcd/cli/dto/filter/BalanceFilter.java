package com.onurbcd.cli.dto.filter;

import com.onurbcd.cli.enums.BalanceType;
import com.onurbcd.cli.util.Extension;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.UUID;

@SuperBuilder
@Getter
@Setter
@ExtensionMethod({Extension.class})
public class BalanceFilter extends AbstractFilterable {

    private LocalDate dayCalendarDate;
    private UUID sourceId;
    private UUID categoryId;
    private BalanceType balanceType;
    private Short dayCalendarYear;
    private Short dayCalendarMonth;
    private Short dayCalendarDayInMonth;

    public static BalanceFilter of(Boolean active, String search, UUID sourceId, UUID categoryId, BalanceType balanceType) {
        return BalanceFilter
                .builder()
                .active(active)
                .search(search)
                .sourceId(sourceId)
                .categoryId(categoryId)
                .balanceType(balanceType)
                .build();
    }

    public BalanceFilter and(Short year, Short month, Short day) {
        this.dayCalendarYear = year.orIfNullCurrentYear();
        this.dayCalendarMonth = month.orIfNullCurrentMonth();
        this.dayCalendarDayInMonth = day;
        return this;
    }
}
