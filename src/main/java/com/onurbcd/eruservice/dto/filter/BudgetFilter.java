package com.onurbcd.eruservice.dto.filter;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

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
                .refYear(refYear)
                .refMonth(refMonth)
                .billTypeId(billTypeId)
                .paid(paid)
                .build();
    }
}
