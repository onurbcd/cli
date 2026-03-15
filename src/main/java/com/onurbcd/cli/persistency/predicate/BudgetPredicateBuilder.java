package com.onurbcd.cli.persistency.predicate;

import com.onurbcd.cli.dto.budget.RefDto;
import com.onurbcd.cli.dto.filter.BudgetFilter;
import com.onurbcd.cli.persistency.entity.QBudget;
import com.querydsl.core.types.Predicate;
import org.springframework.lang.Nullable;

import java.util.UUID;

public class BudgetPredicateBuilder extends BasePredicateBuilder {

    public BudgetPredicateBuilder() {
        super(QBudget.budget.name, QBudget.budget.active);
    }

    public static Predicate id(UUID id) {
        return new BudgetPredicateBuilder().idEq(id).build();
    }

    public static Predicate ref(RefDto refDto) {
        return new BudgetPredicateBuilder().refYear(refDto.getYear()).refMonth(refDto.getMonth()).build();
    }

    public static Predicate all(BudgetFilter filter) {
        return new BudgetPredicateBuilder()
                .refYear(filter.getRefYear())
                .refMonth(filter.getRefMonth())
                .billTypeId(filter.getBillTypeId())
                .paid(filter.getPaid())
                .search(filter.getSearch())
                .active(filter.isActive())
                .build();
    }

    private BudgetPredicateBuilder refYear(@Nullable Short refYear) {
        if (refYear != null) {
            builder().and(QBudget.budget.refYear.eq(refYear));
        }

        return this;
    }

    private BudgetPredicateBuilder refMonth(@Nullable Short refMonth) {
        if (refMonth != null) {
            builder().and(QBudget.budget.refMonth.eq(refMonth));
        }

        return this;
    }

    private BudgetPredicateBuilder billTypeId(@Nullable UUID billTypeId) {
        if (billTypeId != null) {
            builder().and(QBudget.budget.billType.id.eq(billTypeId));
        }

        return this;
    }

    private BudgetPredicateBuilder paid(@Nullable Boolean paid) {
        if (paid != null) {
            builder().and(QBudget.budget.paid.eq(paid));
        }

        return this;
    }

    private BudgetPredicateBuilder idEq(UUID id) {
        builder().and(QBudget.budget.id.eq(id));
        return this;
    }
}
