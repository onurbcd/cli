package com.onurbcd.eruservice.dto.incomesource;

import com.onurbcd.eruservice.dto.PrimeSaveDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.context.ComponentContext;
import org.springframework.validation.annotation.Validated;

import static com.onurbcd.eruservice.util.Constant.NAME_ID;
import static com.onurbcd.eruservice.util.ParamUtil.getBoolean;
import static com.onurbcd.eruservice.util.FlowUtil.getString;
import static com.onurbcd.eruservice.util.StringUtil.normalizeSpace;

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
