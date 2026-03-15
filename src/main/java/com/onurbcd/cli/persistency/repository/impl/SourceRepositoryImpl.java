package com.onurbcd.cli.persistency.repository.impl;

import com.onurbcd.cli.dto.source.SourceDto;
import com.onurbcd.cli.persistency.entity.QSource;
import com.onurbcd.cli.persistency.entity.Source;
import com.onurbcd.cli.persistency.predicate.SourcePredicateBuilder;
import com.onurbcd.cli.persistency.repository.CustomRepository;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAQuery;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.util.UUID;

public class SourceRepositoryImpl implements CustomRepository<SourceDto, Source> {

    @PersistenceContext
    private EntityManager manager;

    @Override
    public SourceDto getSingle(UUID id) {
        var predicate = SourcePredicateBuilder.id(id);
        return columnsQuery(predicate).fetchFirst();
    }

    @Override
    public JPAQuery<Object> mainQuery(Predicate predicate) {
        return new JPAQuery<>(manager)
                .from(QSource.source)
                .innerJoin(QSource.source.incomeSource)
                .where(predicate);
    }

    @Override
    public QBean<SourceDto> columns() {
        return Projections.bean(
                SourceDto.class,
                QSource.source.createdDate,
                QSource.source.lastModifiedDate,
                QSource.source.id,
                QSource.source.name,
                QSource.source.active,
                QSource.source.incomeSource.id.as("incomeSourceId"),
                QSource.source.incomeSource.name.as("incomeSourceName"),
                QSource.source.sourceType,
                QSource.source.currencyType,
                QSource.source.balance
        );
    }

    @Override
    public EntityPathBase<Source> entityPathBase() {
        return QSource.source;
    }

    @Override
    public BigDecimal getSum(Predicate predicate) {
        return mainQuery(predicate)
                .select(Projections.appending(QSource.source.balance.sum()))
                .fetchFirst();
    }
}
