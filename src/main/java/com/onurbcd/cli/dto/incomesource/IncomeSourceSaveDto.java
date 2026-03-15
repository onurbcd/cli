package com.onurbcd.cli.dto.incomesource;

import com.onurbcd.cli.dto.PrimeSaveDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.context.ComponentContext;
import org.springframework.validation.annotation.Validated;

import static com.onurbcd.cli.util.Constant.NAME_ID;
import static com.onurbcd.cli.util.ParamUtil.getBoolean;
import static com.onurbcd.cli.util.FlowUtil.getString;
import static com.onurbcd.cli.util.StringUtil.normalizeSpace;

@SuperBuilder
@Getter
@Setter
@Validated
public class IncomeSourceSaveDto extends PrimeSaveDto {

    public static IncomeSourceSaveDto of(ComponentContext<?> context, @Nullable IncomeSourceDto incomeSource) {
        return IncomeSourceSaveDto.builder()
                .name(normalizeSpace(getString(context, NAME_ID)))
                .active(getBoolean(incomeSource, IncomeSourceDto::isActive))
                .build();
    }
}
