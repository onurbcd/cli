package com.onurbcd.cli.persistency.predicate;

import com.onurbcd.cli.dto.filter.BillTypeFilter;
import com.onurbcd.cli.persistency.entity.QBillType;
import com.querydsl.core.types.Predicate;

import java.util.UUID;

public class BillTypePredicateBuilder extends BasePredicateBuilder {

    public BillTypePredicateBuilder() {
        super(QBillType.billType.name, QBillType.billType.active);
    }

    public static Predicate id(UUID id) {
        return new BillTypePredicateBuilder().idEq(id).build();
    }

    public static Predicate all(BillTypeFilter filter) {
        return new BillTypePredicateBuilder()
                .search(filter.getSearch())
                .active(filter.isActive())
                .build();
    }

    private BillTypePredicateBuilder idEq(UUID id) {
        builder().and(QBillType.billType.id.eq(id));
        return this;
    }
}
