package com.onurbcd.cli.dto.budget;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
public class CopyBudgetDto {

    private RefDto from;
    private RefDto to;

    public static CopyBudgetDto of(Short fromYear, Short fromMonth, Short toYear, Short toMonth) {
        return new CopyBudgetDto(RefDto.of(fromYear, fromMonth), RefDto.of(toYear, toMonth));
    }
}
