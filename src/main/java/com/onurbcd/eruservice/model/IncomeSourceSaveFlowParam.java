package com.onurbcd.eruservice.model;

import java.util.Optional;

import org.springframework.lang.Nullable;
import org.springframework.shell.standard.ShellOption;

import com.onurbcd.eruservice.dto.incomesource.IncomeSourceDto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class IncomeSourceSaveFlowParam {

    private String name;

    public static IncomeSourceSaveFlowParam of(@Nullable IncomeSourceDto incomeSource) {
        return IncomeSourceSaveFlowParam
                .builder()
                .name(Optional.ofNullable(incomeSource).map(IncomeSourceDto::getName).orElse(ShellOption.NULL))
                .build();
    }
}
