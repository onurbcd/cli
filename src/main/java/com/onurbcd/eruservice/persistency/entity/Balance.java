package com.onurbcd.eruservice.persistency.entity;

import com.onurbcd.eruservice.constant.DtoConstant;
import com.onurbcd.eruservice.dto.enums.BalanceType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@AttributeOverride(name = "name", column = @Column(insertable = false, updatable = false))
@Table(
        name = "balance",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uc_balance_sequence_day_id_balance_type",
                        columnNames = {"sequence", "day_id", "balance_type"}
                )
        }
)
public class Balance extends Prime implements SequenceEntity {

    @NotNull
    @Min(1)
    @Column(name = "sequence", nullable = false)
    private Short sequence;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "day_id", nullable = false)
    private Day day;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id", nullable = false)
    private Source source;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    @DecimalMin(DtoConstant.AMOUNT_MIN)
    @DecimalMax(DtoConstant.AMOUNT_MAX)
    private BigDecimal amount;

    @NotNull
    @Size(max = DtoConstant.SIZE_150)
    @Column(name = "code", nullable = false, length = DtoConstant.SIZE_150)
    private String code;

    @Size(max = DtoConstant.SIZE_250)
    @Column(name = "description", length = DtoConstant.SIZE_250)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "balance_type", nullable = false, length = 7)
    private BalanceType balanceType;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "balance_document",
            joinColumns = {@JoinColumn(name = "balance_id", referencedColumnName = "id", nullable = false)},
            inverseJoinColumns = {@JoinColumn(name = "document_id", referencedColumnName = "id", nullable = false)},
            uniqueConstraints = {@UniqueConstraint(name = "uc_balance_document_id", columnNames = {"document_id"})}
    )
    @Builder.Default
    private Set<Document> documents = new HashSet<>();

    public Short getDayInMonth() {
        return day.getCalendarDayInMonth();
    }

    @Override
    public Short getSequenceYear() {
        return day.getCalendarYear();
    }

    @Override
    public Short getSequenceMonth() {
        return day.getCalendarMonth();
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
