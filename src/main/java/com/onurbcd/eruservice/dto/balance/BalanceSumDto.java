package com.onurbcd.eruservice.dto.balance;

import com.onurbcd.eruservice.enums.BalanceSumType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;

@NoArgsConstructor
@Getter
@Setter
public class BalanceSumDto {

    private BalanceSumType type;
    private BigDecimal value;

    private BalanceSumDto(BalanceSumType type, @Nullable BigDecimal value) {
        this.type = type;
        this.value = value != null ? value : BigDecimal.ZERO;
    }

    public static BalanceSumDto income(@Nullable BigDecimal value) {
        return new BalanceSumDto(BalanceSumType.INCOME, value);
    }

    public static BalanceSumDto outcome(@Nullable BigDecimal value) {
        return new BalanceSumDto(BalanceSumType.OUTCOME, value);
    }

    public static BalanceSumDto diff(@Nullable BigDecimal value) {
        return new BalanceSumDto(BalanceSumType.DIFF, value);
    }
}
