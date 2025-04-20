package com.onurbcd.eruservice.dto.budget;

import com.onurbcd.eruservice.dto.PrimeSaveDto;
import com.onurbcd.eruservice.util.Extension;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@SuperBuilder
@Getter
@Setter
@ExtensionMethod({Extension.class})
public class BudgetSaveDto extends PrimeSaveDto {

    private Short sequence;
    private Short refYear;
    private Short refMonth;
    private UUID billTypeId;
    private BigDecimal amount;
    private Boolean paid;

    public static BudgetSaveDto of(String name, Boolean active, String refYear, String refMonth, String billTypeId,
                                   String amount, Boolean paid) {

        return BudgetSaveDto
                .builder()
                .name(name.normalizeSpace())
                .active(active)
                .sequence(null)
                .refYear(Short.parseShort(refYear))
                .refMonth(Short.parseShort(refMonth))
                .billTypeId(UUID.fromString(billTypeId))
                .amount(new BigDecimal(amount))
                .paid(paid)
                .build();
    }
}
