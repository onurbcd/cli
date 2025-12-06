package com.onurbcd.eruservice.dto.incomesource;

import static com.onurbcd.eruservice.util.Constant.NAME;

import java.util.Optional;

import org.springframework.lang.Nullable;
import org.springframework.shell.component.context.ComponentContext;
import org.springframework.validation.annotation.Validated;

import com.onurbcd.eruservice.dto.PrimeSaveDto;
import com.onurbcd.eruservice.util.FlowUtil;
import com.onurbcd.eruservice.util.StringUtil;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
@Validated
public class IncomeSourceSaveDto extends PrimeSaveDto {

    public static IncomeSourceSaveDto of(ComponentContext<?> context, @Nullable IncomeSourceDto incomeSource) {
        return IncomeSourceSaveDto
                .builder()
                .name(StringUtil.normalizeSpace(FlowUtil.getString(context, NAME)))
                .active(Optional.ofNullable(incomeSource).map(IncomeSourceDto::isActive).orElse(Boolean.TRUE))
                .build();
    }
}
