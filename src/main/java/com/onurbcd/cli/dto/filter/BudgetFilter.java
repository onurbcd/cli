package com.onurbcd.cli.dto.filter;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Clock;
import java.util.UUID;

import static com.onurbcd.cli.util.DateUtil.orCurrentYearAndMonth;

@SuperBuilder
@Getter
@Setter
public class BudgetFilter extends AbstractFilterable {

    private Short refYear;
    private Short refMonth;
    private UUID billTypeId;
    private Boolean paid;

    public static BudgetFilter of(Boolean active, String search, Short refYear, Short refMonth, UUID billTypeId,
                                  Boolean paid, Clock clock) {

        var reference = orCurrentYearAndMonth(refYear, refMonth, clock);

        return BudgetFilter
                .builder()
                .active(active)
                .search(search)
                .refYear(reference.year())
                .refMonth(reference.month())
                .billTypeId(billTypeId)
                .paid(paid)
                .build();
    }
}
