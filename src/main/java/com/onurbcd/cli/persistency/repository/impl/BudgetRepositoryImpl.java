package com.onurbcd.cli.persistency.repository.impl;

import com.onurbcd.cli.dto.budget.BudgetDto;
import com.onurbcd.cli.persistency.entity.Budget;
import com.onurbcd.cli.persistency.entity.QBudget;
import com.onurbcd.cli.persistency.predicate.BudgetPredicateBuilder;
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

public class BudgetRepositoryImpl implements CustomRepository<BudgetDto, Budget> {

    @PersistenceContext
    private EntityManager manager;

    @Override
    public BudgetDto getSingle(UUID id) {
        var predicate = BudgetPredicateBuilder.id(id);
        return columnsQuery(predicate).fetchFirst();
    }

    @Override
    public JPAQuery<Object> mainQuery(Predicate predicate) {
        return new JPAQuery<>(manager)
                .from(QBudget.budget)
                .innerJoin(QBudget.budget.billType)
                .where(predicate);
    }

    @Override
    public QBean<BudgetDto> columns() {
        return Projections.bean(
                BudgetDto.class,
                QBudget.budget.createdDate,
                QBudget.budget.lastModifiedDate,
                QBudget.budget.id,
                QBudget.budget.name,
                QBudget.budget.active,
                QBudget.budget.sequence,
                QBudget.budget.refYear,
                QBudget.budget.refMonth,
                QBudget.budget.billType.id.as("billTypeId"),
                QBudget.budget.billType.name.as("billTypeName"),
                QBudget.budget.amount,
                QBudget.budget.paid
        );
    }

    @Override
    public EntityPathBase<Budget> entityPathBase() {
        return QBudget.budget;
    }

    @Override
    public BigDecimal getSum(Predicate predicate) {
        return BigDecimal.ZERO;
    }
}
