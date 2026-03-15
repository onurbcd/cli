package com.onurbcd.cli.persistency.repository;

import com.onurbcd.cli.dto.Dtoable;
import com.onurbcd.cli.persistency.entity.Entityable;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.repository.NoRepositoryBean;

import java.math.BigDecimal;
import java.util.UUID;

@NoRepositoryBean
public interface MiddlewareRepository<E extends Entityable, D extends Dtoable> extends EruRepository<E, D> {

    @Override
    default D getSingle(UUID id) {
        return null;
    }

    @Override
    default JPAQuery<Object> mainQuery(Predicate predicate) {
        return new JPAQuery<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    default QBean<D> columns() {
        return (QBean<D>) Projections.bean(Dtoable.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    default EntityPathBase<E> entityPathBase() {
        return new EntityPathBase<>((Class<? extends E>) Entityable.class, StringUtils.EMPTY);
    }

    @Override
    default BigDecimal getSum(Predicate predicate) {
        return BigDecimal.ZERO;
    }
}
