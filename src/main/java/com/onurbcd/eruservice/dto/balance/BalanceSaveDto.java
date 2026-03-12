package com.onurbcd.eruservice.dto.balance;

import com.onurbcd.eruservice.dto.PrimeSaveDto;
import com.onurbcd.eruservice.enums.BalanceType;
import com.onurbcd.eruservice.util.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.context.ComponentContext;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.onurbcd.eruservice.util.Constant.*;

@SuperBuilder
@Getter
@Setter
@Validated
public class BalanceSaveDto extends PrimeSaveDto {

    @Min(value = 1, message = "Sequence must be greater than 0.")
    private Short sequence;

    @NotNull(message = "Day calendar date is required.")
    private LocalDate dayCalendarDate;

    @NotNull(message = "Source is required.")
    private UUID sourceId;

    @NotNull(message = "Category is required.")
    private UUID categoryId;

    @NotNull(message = "Amount is required.")
    @DecimalMin(value = Constant.AMOUNT_MIN, message = "Amount must be greater than {value}")
    @DecimalMax(value = Constant.AMOUNT_MAX, message = "Amount must be less than {value}")
    private BigDecimal amount;

    @Size(max = 150, message = "Code must be less than {max} characters.")
    private String code;

    @Size(max = 250, message = "Description must be less than {max} characters.")
    private String description;

    @NotNull(message = "Balance type is required.")
    private BalanceType balanceType;

    private Set<UUID> documentsIds;
    private List<String> filesNames;

    public static BalanceSaveDto of(ComponentContext<?> context, @Nullable BalanceDto balance) {
        var documentsIdsList = FlowUtil.getStringList(context, DOCUMENTS_IDS_ID);
        Set<UUID> documentsIds;

        if (documentsIdsList != null) {
            documentsIds = documentsIdsList.stream()
                    .map(Converter::toUUID)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toSet());
        } else {
            documentsIds = Optional.ofNullable(balance).map(BalanceDto::getDocumentsIds).orElse(null);
        }

        return BalanceSaveDto
                .builder()
                .name(Constant.BOGUS_NAME)
                .active(Optional.ofNullable(balance).map(BalanceDto::isActive).orElse(Boolean.TRUE))
                .sequence(Optional.ofNullable(balance).map(BalanceDto::getSequence).orElse(null))
                .dayCalendarDate(DateUtil.parseLocalDate(FlowUtil.getString(context, DAY_ID)))
                .sourceId(Converter.toUUID(FlowUtil.getString(context, SOURCE_ID)))
                .categoryId(Converter.toUUID(FlowUtil.getString(context, CATEGORY_ID)))
                .amount(NumberUtil.toBigDecimal(FlowUtil.getString(context, AMOUNT_ID)))
                .code(StringUtil.normalizeSpace(FlowUtil.getString(context, CODE_ID)))
                .description(StringUtil.normalizeSpace(FlowUtil.getString(context, DESCRIPTION_ID)))
                .balanceType(EnumUtil.valueOf(BalanceType.class, FlowUtil.getString(context, BALANCE_TYPE_ID)))
                .documentsIds(documentsIds)
                .filesNames(FlowUtil.getStringList(context, DOCUMENTS_ID))
                .build();
    }

    public short getYear() {
        return (short) dayCalendarDate.getYear();
    }

    public short getMonth() {
        return (short) dayCalendarDate.getMonthValue();
    }

    public short getDay() {
        return (short) dayCalendarDate.getDayOfMonth();
    }
}
