package com.onurbcd.cli.dto.filter;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class IncomeSourceFilter extends AbstractFilterable {

    public static IncomeSourceFilter of(Boolean active, String search) {
        return IncomeSourceFilter
                .builder()
                .active(active)
                .search(search)
                .build();
    }
}
