package com.onurbcd.cli.dto.source;

import com.onurbcd.cli.dto.PrimeDto;
import com.onurbcd.cli.enums.CurrencyType;
import com.onurbcd.cli.enums.SourceType;
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
