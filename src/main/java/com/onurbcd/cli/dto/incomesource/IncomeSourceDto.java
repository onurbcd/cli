package com.onurbcd.cli.dto.incomesource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.onurbcd.cli.dto.PrimeDto;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IncomeSourceDto extends PrimeDto {
}
