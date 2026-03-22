package com.onurbcd.cli.dto.budget;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.onurbcd.cli.dto.PrimeDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class BudgetDto extends PrimeDto {

    private Short sequence;
    private Short refYear;
    private Short refMonth;
    private UUID billTypeId;
    private String billTypeName;
    private BigDecimal amount;
    private Boolean paid;
}
