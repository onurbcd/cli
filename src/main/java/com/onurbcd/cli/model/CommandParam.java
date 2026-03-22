package com.onurbcd.cli.model;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class CommandParam {

    private UUID id;
    private Short year;
    private Short month;

    public static CommandParam of(UUID id) {
        return CommandParam.builder()
                .id(id)
                .build();
    }

    public static CommandParam of(Short year, Short month) {
        return CommandParam.builder()
                .year(year)
                .month(month)
                .build();
    }
}
