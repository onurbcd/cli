package com.onurbcd.cli.persistency.repository.impl;

import com.onurbcd.cli.dto.balance.BalanceDto;
import com.onurbcd.cli.persistency.entity.Balance;
import com.onurbcd.cli.persistency.entity.QBalance;
import com.onurbcd.cli.persistency.predicate.BalancePredicateBuilder;
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

public class BalanceRepositoryImpl implements CustomRepository<BalanceDto, Balance> {

    @PersistenceContext
    private EntityManager manager;

    @Override
    public BalanceDto getSingle(UUID id) {
        var predicate = BalancePredicateBuilder.id(id);
        return columnsQuery(predicate).fetchFirst();
    }

    @Override
    public JPAQuery<Object> mainQuery(Predicate predicate) {
        return new JPAQuery<>(manager)
                .from(QBalance.balance)
                .innerJoin(QBalance.balance.day)
                .innerJoin(QBalance.balance.source)
                .innerJoin(QBalance.balance.category)
                .where(predicate);
    }

    @Override
    public QBean<BalanceDto> columns() {
        return Projections.bean(
                BalanceDto.class,
                QBalance.balance.createdDate,
                QBalance.balance.lastModifiedDate,
                QBalance.balance.id,
                QBalance.balance.name,
                QBalance.balance.active,
                QBalance.balance.sequence,
                QBalance.balance.day.id.as("dayId"),
                QBalance.balance.day.calendarDate.as("dayCalendarDate"),
                QBalance.balance.source.id.as("sourceId"),
                QBalance.balance.source.name.as("sourceName"),
                QBalance.balance.category.id.as("categoryId"),
                QBalance.balance.category.name.as("categoryName"),
                QBalance.balance.amount,
                QBalance.balance.code,
                QBalance.balance.description,
                QBalance.balance.balanceType,
                QBalance.balance.paymentType
        );
    }

    @Override
    public EntityPathBase<Balance> entityPathBase() {
        return QBalance.balance;
    }

    @Override
    public BigDecimal getSum(Predicate predicate) {
        return mainQuery(predicate)
                .select(Projections.appending(QBalance.balance.amount.sum()))
                .fetchFirst();
    }
}
