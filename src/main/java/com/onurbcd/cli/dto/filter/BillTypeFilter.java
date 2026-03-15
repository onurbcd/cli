package com.onurbcd.cli.dto.filter;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class BillTypeFilter extends AbstractFilterable {

    public static BillTypeFilter of(Boolean active, String search) {
        return BillTypeFilter
                .builder()
                .active(active)
                .search(search)
                .build();
    }
}
