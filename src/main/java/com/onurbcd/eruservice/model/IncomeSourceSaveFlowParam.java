package com.onurbcd.eruservice.model;

import com.onurbcd.eruservice.dto.incomesource.IncomeSourceDto;
import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.Nullable;

import static com.onurbcd.eruservice.util.FlowParamUtil.getString;

@Builder
@Getter
public class IncomeSourceSaveFlowParam {

    private String name;

    public static IncomeSourceSaveFlowParam of(@Nullable IncomeSourceDto incomeSource) {
        return IncomeSourceSaveFlowParam.builder()
                .name(getString(incomeSource, IncomeSourceDto::getName))
                .build();
    }
}
