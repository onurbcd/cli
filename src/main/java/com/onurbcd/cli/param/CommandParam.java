package com.onurbcd.cli.param;

import com.onurbcd.cli.enums.FlowType;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class CommandParam {

    private UUID id;
    private FlowType flowType;
    private Short year;
    private Short month;

    public static CommandParam of(UUID id) {
        return CommandParam.builder()
                .id(id)
                .build();
    }

    public static CommandParam of(FlowType flowType, Short year, Short month) {
        return CommandParam.builder()
                .flowType(flowType)
                .year(year)
                .month(month)
                .build();
    }
}
