package com.onurbcd.cli.persistency.predicate;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.StringPath;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

public abstract class BasePredicateBuilder {

    private final BooleanBuilder builder;

    private final StringPath namePath;

    private final BooleanPath activePath;

    protected BasePredicateBuilder(StringPath namePath, BooleanPath activePath) {
        this.builder = new BooleanBuilder();
        this.namePath = namePath;
        this.activePath = activePath;
    }

    protected BooleanBuilder builder() {
        return builder;
    }

    public Predicate build() {
        return builder;
    }

    public BasePredicateBuilder search(@Nullable String search) {
        if (StringUtils.isNotBlank(search)) {
            builder().and(namePath.containsIgnoreCase(search));
        }

        return this;
    }

    public BasePredicateBuilder active(@Nullable Boolean active) {
        if (active != null) {
            builder().and(activePath.eq(active));
        }

        return this;
    }
}
