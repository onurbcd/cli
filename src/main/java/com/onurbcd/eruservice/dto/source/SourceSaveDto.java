package com.onurbcd.eruservice.dto.source;

import static com.onurbcd.eruservice.util.Constant.BALANCE;
import static com.onurbcd.eruservice.util.Constant.CURRENCY_TYPE;
import static com.onurbcd.eruservice.util.Constant.INCOME_SOURCE_ID;
import static com.onurbcd.eruservice.util.Constant.NAME;
import static com.onurbcd.eruservice.util.Constant.SOURCE_TYPE;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.springframework.lang.Nullable;
import org.springframework.shell.component.context.ComponentContext;
import org.springframework.validation.annotation.Validated;

import com.onurbcd.eruservice.dto.PrimeSaveDto;
import com.onurbcd.eruservice.enums.CurrencyType;
import com.onurbcd.eruservice.enums.SourceType;
import com.onurbcd.eruservice.util.Constant;
import com.onurbcd.eruservice.util.Converter;
import com.onurbcd.eruservice.util.EnumUtil;
import com.onurbcd.eruservice.util.FlowUtil;
import com.onurbcd.eruservice.util.NumberUtil;
import com.onurbcd.eruservice.util.StringUtil;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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
        return SourceSaveDto
                .builder()
                .name(StringUtil.normalizeSpace(FlowUtil.getString(context, NAME)))
                .active(Optional.ofNullable(source).map(SourceDto::isActive).orElse(Boolean.TRUE))
                .incomeSourceId(Converter.toUUID(FlowUtil.getString(context, INCOME_SOURCE_ID)))
                .sourceType(EnumUtil.valueOf(SourceType.class, FlowUtil.getString(context, SOURCE_TYPE)))
                .currencyType(EnumUtil.valueOf(CurrencyType.class, FlowUtil.getString(context, CURRENCY_TYPE)))
                .balance(NumberUtil.toBigDecimal(FlowUtil.getString(context, BALANCE)))
                .build();
    }
}
