package com.onurbcd.eruservice.dto.budget;

import com.onurbcd.eruservice.annotation.MaxYear;
import com.onurbcd.eruservice.annotation.MinYear;
import com.onurbcd.eruservice.dto.PrimeSaveDto;
import com.onurbcd.eruservice.util.Constant;
import com.onurbcd.eruservice.util.FlowUtil;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.context.ComponentContext;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.UUID;

import static com.onurbcd.eruservice.util.Constant.*;
import static com.onurbcd.eruservice.util.Converter.toUUID;
import static com.onurbcd.eruservice.util.FlowUtil.getString;
import static com.onurbcd.eruservice.util.NumberUtil.parseShort;
import static com.onurbcd.eruservice.util.NumberUtil.toBigDecimal;
import static com.onurbcd.eruservice.util.ParamUtil.getBoolean;
import static com.onurbcd.eruservice.util.ParamUtil.getNullShort;
import static com.onurbcd.eruservice.util.StringUtil.normalizeSpace;

@SuperBuilder
@Getter
@Setter
@Validated
public class BudgetSaveDto extends PrimeSaveDto {

    @Min(value = 1, message = "Sequence must be greater than {min}")
    private Short sequence;

    @NotNull(message = "Reference year is mandatory")
    @MinYear(message = "Reference year must be {value} or greater")
    @MaxYear(message = "Reference year must be {value} or less")
    private Short refYear;

    @NotNull(message = "Reference month is mandatory")
    @Min(value = 1, message = "Reference month must be greater than or equal to {value}")
    @Max(value = 12, message = "Reference month must be less than or equal to {value}")
    private Short refMonth;

    @NotNull(message = "Bill type is mandatory")
    private UUID billTypeId;

    @NotNull(message = "Amount is mandatory")
    @DecimalMin(value = Constant.POSITIVE_AMOUNT_MIN, message = "Amount must be greater than or equal to {value}")
    @DecimalMax(value = Constant.AMOUNT_MAX, message = "Amount must be less than or equal to {value}")
    private BigDecimal amount;

    @NotNull(message = "Paid status is mandatory")
    private Boolean paid;

    public static BudgetSaveDto of(ComponentContext<?> context, @Nullable BudgetDto budgetDto) {
        return BudgetSaveDto.builder()
                .name(normalizeSpace(getString(context, NAME_ID)))
                .active(getBoolean(budgetDto, BudgetDto::isActive))
                .sequence(getNullShort(budgetDto, BudgetDto::getSequence))
                .refYear(parseShort(getString(context, REF_YEAR_ID)))
                .refMonth(parseShort(getString(context, REF_MONTH_ID)))
                .billTypeId(toUUID(getString(context, BILL_TYPE_ID_ID)))
                .amount(toBigDecimal(getString(context, AMOUNT_ID)))
                .paid(FlowUtil.getBoolean(context, PAID_ID))
                .build();
    }
}
