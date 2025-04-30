package com.onurbcd.eruservice.dto.source;

import com.onurbcd.eruservice.dto.PrimeSaveDto;
import com.onurbcd.eruservice.enums.CurrencyType;
import com.onurbcd.eruservice.dto.enums.SourceType;
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
public class SourceSaveDto extends PrimeSaveDto {

    private UUID incomeSourceId;
    private SourceType sourceType;
    private CurrencyType currencyType;
    private BigDecimal balance;

    public static SourceSaveDto of(String name, Boolean active, String incomeSourceId, String sourceType,
                                   String currencyType, String balance) {

        return SourceSaveDto
                .builder()
                .name(name.normalizeSpace())
                .active(active)
                .incomeSourceId(UUID.fromString(incomeSourceId))
                .sourceType(SourceType.valueOf(sourceType))
                .currencyType(CurrencyType.valueOf(currencyType))
                .balance(new BigDecimal(balance))
                .build();
    }
}
