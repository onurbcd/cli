package com.onurbcd.cli.persistency.predicate;

import com.onurbcd.cli.persistency.entity.QIncomeSource;

public class IncomeSourcePredicateBuilder extends BasePredicateBuilder {

    public IncomeSourcePredicateBuilder() {
        super(QIncomeSource.incomeSource.name, QIncomeSource.incomeSource.active);
    }
}
