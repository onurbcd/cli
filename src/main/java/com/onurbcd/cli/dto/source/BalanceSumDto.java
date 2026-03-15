package com.onurbcd.cli.dto.source;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;

@NoArgsConstructor
@Getter
@Setter
public class BalanceSumDto {

    private BigDecimal partial;
    private BigDecimal total;

    public BalanceSumDto(@Nullable BigDecimal partial, @Nullable BigDecimal total) {
        this.partial = partial != null ? partial : BigDecimal.ZERO;
        this.total = total != null ? total : BigDecimal.ZERO;
    }
}
