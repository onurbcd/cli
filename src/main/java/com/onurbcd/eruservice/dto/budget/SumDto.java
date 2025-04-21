package com.onurbcd.eruservice.dto.budget;

import com.onurbcd.eruservice.dto.enums.SumType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;

@NoArgsConstructor
@Getter
@Setter
public class SumDto {

    private SumType type;

    private BigDecimal value;

    private SumDto(SumType type, @Nullable BigDecimal value) {
        this.type = type;
        this.value = value != null ? value : BigDecimal.ZERO;
    }

    public static SumDto total(@Nullable BigDecimal value) {
        return new SumDto(SumType.TOTAL, value);
    }

    public static SumDto paid(@Nullable BigDecimal value) {
        return new SumDto(SumType.PAID, value);
    }

    public static SumDto unpaid(@Nullable BigDecimal value) {
        return new SumDto(SumType.UNPAID, value);
    }

    public static SumDto balance(BigDecimal value) {
        return new SumDto(SumType.BALANCE, value);
    }
}
