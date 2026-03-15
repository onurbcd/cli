package com.onurbcd.cli.dto.source;

import com.onurbcd.cli.dto.PrimeSaveDto;
import com.onurbcd.cli.enums.CurrencyType;
import com.onurbcd.cli.enums.SourceType;
import com.onurbcd.cli.util.Constant;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.context.ComponentContext;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.UUID;

import static com.onurbcd.cli.util.Constant.*;
import static com.onurbcd.cli.util.Converter.toUUID;
import static com.onurbcd.cli.util.EnumUtil.valueOf;
import static com.onurbcd.cli.util.FlowUtil.getString;
import static com.onurbcd.cli.util.NumberUtil.toBigDecimal;
import static com.onurbcd.cli.util.ParamUtil.getBoolean;
import static com.onurbcd.cli.util.StringUtil.normalizeSpace;

@SuperBuilder
@Getter
@Setter
@Validated
public class SourceSaveDto extends PrimeSaveDto {

    @NotNull(message = "Income source id is required.")
    private UUID incomeSourceId;

    @NotNull(message = "Source type is required.")
    private SourceType sourceType;

    @NotNull(message = "Currency type is required.")
    private CurrencyType currencyType;

    @NotNull(message = "Balance is required.")
    @DecimalMin(value = Constant.AMOUNT_MIN, message = "Balance must be greater than or equal to {value}.")
    @DecimalMax(value = Constant.AMOUNT_MAX, message = "Balance must be less than or equal to {value}.")
    private BigDecimal balance;

    public static SourceSaveDto of(ComponentContext<?> context, @Nullable SourceDto source) {
        return SourceSaveDto.builder()
                .name(normalizeSpace(getString(context, NAME_ID)))
                .active(getBoolean(source, SourceDto::isActive))
                .incomeSourceId(toUUID(getString(context, INCOME_SOURCE_ID_ID)))
                .sourceType(valueOf(SourceType.class, getString(context, SOURCE_TYPE_ID)))
                .currencyType(valueOf(CurrencyType.class, getString(context, CURRENCY_TYPE_ID)))
                .balance(toBigDecimal(getString(context, BALANCE_ID)))
                .build();
    }
}
