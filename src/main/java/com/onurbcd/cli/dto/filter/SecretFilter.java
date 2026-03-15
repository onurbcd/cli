package com.onurbcd.cli.dto.filter;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class SecretFilter extends AbstractFilterable {

    public static SecretFilter of(Boolean active, String search) {
        return SecretFilter
                .builder()
                .active(active)
                .search(search)
                .build();
    }
}
