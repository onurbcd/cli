package com.onurbcd.cli.dto.filter;

import com.onurbcd.cli.util.Extension;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@SuperBuilder
@Getter
@Setter
@ExtensionMethod({Extension.class})
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
                .refYear(refYear.orIfNullCurrentYear())
                .refMonth(refMonth.orIfNullCurrentMonth())
                .billTypeId(billTypeId)
                .paid(paid)
                .build();
    }
}
