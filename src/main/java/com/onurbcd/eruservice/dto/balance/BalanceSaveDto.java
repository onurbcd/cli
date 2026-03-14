package com.onurbcd.eruservice.dto.balance;

import com.onurbcd.eruservice.dto.PrimeSaveDto;
import com.onurbcd.eruservice.enums.BalanceType;
import com.onurbcd.eruservice.model.MultipartFile;
import com.onurbcd.eruservice.util.Constant;
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
import java.util.Set;
import java.util.UUID;

import static com.onurbcd.eruservice.util.Constant.*;
import static com.onurbcd.eruservice.util.Converter.toUUID;
import static com.onurbcd.eruservice.util.DateUtil.parseLocalDate;
import static com.onurbcd.eruservice.util.EnumUtil.valueOf;
import static com.onurbcd.eruservice.util.FileUtil.filesToMultipartFiles;
import static com.onurbcd.eruservice.util.FlowUtil.getString;
import static com.onurbcd.eruservice.util.FlowUtil.getStringList;
import static com.onurbcd.eruservice.util.NumberUtil.toBigDecimal;
import static com.onurbcd.eruservice.util.ParamUtil.*;
import static com.onurbcd.eruservice.util.StringUtil.normalizeSpace;

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
    private List<MultipartFile> multipartFiles;

    public static BalanceSaveDto of(ComponentContext<?> context, @Nullable BalanceDto balance) {
        var documentsIdsList = getStringList(context, DOCUMENTS_IDS_ID);

        var documentsIds = documentsIdsList != null
                ? getUUIDSet(documentsIdsList)
                : getNullUUIDSet(balance, BalanceDto::getDocumentsIds);

        return BalanceSaveDto.builder()
                .name(Constant.BOGUS_NAME)
                .active(getBoolean(balance, BalanceDto::isActive))
                .sequence(getNullShort(balance, BalanceDto::getSequence))
                .dayCalendarDate(parseLocalDate(getString(context, DAY_ID)))
                .sourceId(toUUID(getString(context, SOURCE_ID)))
                .categoryId(toUUID(getString(context, CATEGORY_ID)))
                .amount(toBigDecimal(getString(context, AMOUNT_ID)))
                .code(normalizeSpace(getString(context, CODE_ID)))
                .description(normalizeSpace(getString(context, DESCRIPTION_ID)))
                .balanceType(valueOf(BalanceType.class, getString(context, BALANCE_TYPE_ID)))
                .documentsIds(documentsIds)
                .multipartFiles(filesToMultipartFiles(getStringList(context, DOCUMENTS_ID)))
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
