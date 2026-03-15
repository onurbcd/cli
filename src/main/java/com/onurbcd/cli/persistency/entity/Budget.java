package com.onurbcd.cli.persistency.entity;

import com.onurbcd.cli.annotation.MaxYear;
import com.onurbcd.cli.annotation.MinYear;
import com.onurbcd.cli.util.Constant;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@AttributeOverride(name = "name", column = @Column())
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "name", "refYear", "refMonth"
        }),
        @UniqueConstraint(columnNames = {
                "sequence", "refYear", "refMonth"
        })
})
public class Budget extends Prime implements SequenceEntity {

    @NotNull
    @Min(1)
    private Short sequence;

    @NotNull
    @MinYear
    @MaxYear
    private Short refYear;

    @NotNull
    @Min(1)
    @Max(12)
    private Short refMonth;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private BillType billType;

    @NotNull
    @Column(precision = 19, scale = 4)
    @DecimalMin(Constant.POSITIVE_AMOUNT_MIN)
    @DecimalMax(Constant.AMOUNT_MAX)
    private BigDecimal amount;

    @NotNull
    private Boolean paid;

    @Override
    public Short getSequenceYear() {
        return refYear;
    }

    @Override
    public Short getSequenceMonth() {
        return refMonth;
    }

    @Override
    public Short getSequenceValue() {
        return sequence;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
