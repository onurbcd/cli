package com.onurbcd.cli.dto.source;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SourceDto extends PrimeDto {

    private UUID incomeSourceId;
    private String incomeSourceName;
    private SourceType sourceType;
    private CurrencyType currencyType;
    private BigDecimal balance;
}
