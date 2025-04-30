package com.onurbcd.eruservice.persistency.predicate;

import com.onurbcd.eruservice.enums.CurrencyType;
import com.onurbcd.eruservice.enums.SourceType;
import com.onurbcd.eruservice.dto.filter.SourceFilter;
import com.onurbcd.eruservice.persistency.entity.QSource;
import com.querydsl.core.types.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

import java.util.UUID;

public class SourcePredicateBuilder extends BasePredicateBuilder {

    public SourcePredicateBuilder() {
        super(QSource.source.name, QSource.source.active);
    }

    public static Predicate id(UUID id) {
        return new SourcePredicateBuilder().idEq(id).build();
    }

    public static Predicate all(SourceFilter filter) {
        return new SourcePredicateBuilder()
                .incomeSourceId(filter.getIncomeSourceId())
                .sourceType(filter.getSourceType())
                .currencyType(filter.getCurrencyType())
                .search(filter.getSearch())
                .active(filter.isActive())
                .build();
    }

    @Override
    public SourcePredicateBuilder search(String search) {
        if (StringUtils.isNotBlank(search)) {
            builder().and(QSource.source.name.containsIgnoreCase(search)
                    .or(QSource.source.incomeSource.name.containsIgnoreCase(search)));
        }

        return this;
    }

    private SourcePredicateBuilder idEq(UUID id) {
        builder().and(QSource.source.id.eq(id));
        return this;
    }

    private SourcePredicateBuilder incomeSourceId(@Nullable UUID incomeSourceId) {
        if (incomeSourceId != null) {
            builder().and(QSource.source.incomeSource.id.eq(incomeSourceId));
        }

        return this;
    }

    private SourcePredicateBuilder sourceType(@Nullable SourceType sourceType) {
        if (sourceType != null) {
            builder().and(QSource.source.sourceType.eq(sourceType));
        }

        return this;
    }

    private SourcePredicateBuilder currencyType(@Nullable CurrencyType currencyType) {
        if (currencyType != null) {
            builder().and(QSource.source.currencyType.eq(currencyType));
        }

        return this;
    }
}
