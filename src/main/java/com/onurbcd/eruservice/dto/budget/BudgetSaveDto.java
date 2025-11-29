package com.onurbcd.eruservice.dto.budget;

import java.math.BigDecimal;
import java.util.UUID;

import com.onurbcd.eruservice.dto.PrimeSaveDto;
import com.onurbcd.eruservice.util.Converter;
import com.onurbcd.eruservice.util.Extension;
import com.onurbcd.eruservice.util.NumberUtil;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
@ExtensionMethod({ Extension.class })
public class BudgetSaveDto extends PrimeSaveDto {

    private Short sequence;
    private Short refYear;
    private Short refMonth;
    private UUID billTypeId;
    private BigDecimal amount;
    private Boolean paid;

    public static BudgetSaveDto of(String name, Boolean active, Short sequence, String refYear, String refMonth,
            String billTypeId, String amount, Boolean paid) {

        return BudgetSaveDto
                .builder()
                .name(name.normalizeSpace())
                .active(active)
                .sequence(sequence)
                .refYear(NumberUtil.parseShort(refYear))
                .refMonth(NumberUtil.parseShort(refMonth))
                .billTypeId(Converter.toUUID(billTypeId))
                .amount(NumberUtil.toBigDecimal(amount))
                .paid(paid)
                .build();
    }
}
