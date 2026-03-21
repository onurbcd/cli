package com.onurbcd.cli.dto.filter;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

import static com.onurbcd.cli.util.DateUtil.orCurrentMonth;
import static com.onurbcd.cli.util.DateUtil.orCurrentYear;

@SuperBuilder
@Getter
@Setter
public class BudgetFilter extends AbstractFilterable {

    private Short refYear;
    private Short refMonth;
    private UUID billTypeId;
    private Boolean paid;

    public static BudgetFilter of(Boolean active, String search, Short refYear, Short refMonth, UUID billTypeId,
                                  Boolean paid) {

        return BudgetFilter
                .builder()
                .active(active)
                .search(search)
                .refYear(orCurrentYear(refYear))
                .refMonth(orCurrentMonth(refMonth))
                .billTypeId(billTypeId)
                .paid(paid)
                .build();
    }
}
