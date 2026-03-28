package com.onurbcd.cli.param.flow;

import com.onurbcd.cli.dto.incomesource.IncomeSourceDto;
import com.onurbcd.cli.enums.FlowType;
import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.Nullable;

import static com.onurbcd.cli.util.ParamUtil.getString;

@Builder
@Getter
public class IncomeSourceSaveFlowParam implements Paramable {

    private String name;

    public static IncomeSourceSaveFlowParam of(@Nullable IncomeSourceDto incomeSource) {
        return IncomeSourceSaveFlowParam.builder()
                .name(getString(incomeSource, IncomeSourceDto::getName))
                .build();
    }

    @Override
    public FlowType getType() {
        return FlowType.INCOME_SOURCE;
    }
}
