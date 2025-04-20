package com.onurbcd.eruservice.dto.budget;

import com.onurbcd.eruservice.dto.PrimeDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class BudgetDto extends PrimeDto {

    private Short sequence;
    private Short refYear;
    private Short refMonth;
    private UUID billTypeId;
    private String billTypeName;
    private BigDecimal amount;
    private Boolean paid;
}
