package com.onurbcd.eruservice.dto.source;

import com.onurbcd.eruservice.dto.PrimeDto;
import com.onurbcd.eruservice.enums.CurrencyType;
import com.onurbcd.eruservice.dto.enums.SourceType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class SourceDto extends PrimeDto {

    private UUID incomeSourceId;
    private String incomeSourceName;
    private SourceType sourceType;
    private CurrencyType currencyType;
    private BigDecimal balance;
}
