package com.onurbcd.eruservice.persistency.entity;

import com.onurbcd.eruservice.enums.CurrencyType;
import com.onurbcd.eruservice.enums.SourceType;
import com.onurbcd.eruservice.util.Constant;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "source")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Source extends Prime {

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "income_source_id", nullable = false)
    private IncomeSource incomeSource;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 8)
    private SourceType sourceType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "currency_type", nullable = false, length = 7)
    private CurrencyType currencyType;

    @NotNull
    @Column(name = "balance", nullable = false, precision = 19, scale = 4)
    @DecimalMin(Constant.AMOUNT_MIN)
    @DecimalMax(Constant.AMOUNT_MAX)
    private BigDecimal balance;

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
