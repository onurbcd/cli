package com.onurbcd.eruservice.dto.balance;

import com.onurbcd.eruservice.dto.PrimeSaveDto;
import com.onurbcd.eruservice.dto.enums.BalanceType;
import com.onurbcd.eruservice.util.DateUtil;
import com.onurbcd.eruservice.util.Extension;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.SuperBuilder;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@SuperBuilder
@Getter
@Setter
@ExtensionMethod({Extension.class})
public class BalanceSaveDto extends PrimeSaveDto {

    private Short sequence;
    private LocalDate dayCalendarDate;
    private UUID sourceId;
    private UUID categoryId;
    private BigDecimal amount;
    private String code;
    private String description;
    private BalanceType balanceType;
    private Set<UUID> documentsIds;
    private List<String> filesNames;

    public static BalanceSaveDto of(String name, Boolean active, @Nullable Short sequence, String dayCalendarDate,
                                    String sourceId, String categoryId, String amount, String code,
                                    @Nullable String description, String balanceType, @Nullable Set<UUID> documentsIds,
                                    @Nullable List<String> filesNames) {
        return BalanceSaveDto
                .builder()
                .name(name.normalizeSpace())
                .active(active)
                .sequence(sequence)
                .dayCalendarDate(DateUtil.parseLocalDate(dayCalendarDate))
                .sourceId(UUID.fromString(sourceId))
                .categoryId(UUID.fromString(categoryId))
                .amount(new BigDecimal(amount))
                .code(code.normalizeSpace())
                .description(description.defaultToNull())
                .balanceType(BalanceType.valueOf(balanceType))
                .documentsIds(documentsIds)
                .filesNames(filesNames)
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
