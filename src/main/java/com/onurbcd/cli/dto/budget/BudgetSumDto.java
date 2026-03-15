package com.onurbcd.cli.dto.budget;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BudgetSumDto {

    private BigDecimal amount;

    private Boolean paid;

    private Long size;
}
