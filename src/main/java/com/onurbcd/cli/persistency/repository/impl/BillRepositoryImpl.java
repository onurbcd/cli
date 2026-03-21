package com.onurbcd.cli.persistency.repository.impl;

import com.onurbcd.cli.dto.bill.BillDto;
import com.onurbcd.cli.persistency.entity.Bill;
import com.onurbcd.cli.persistency.entity.QBill;
import com.onurbcd.cli.persistency.predicate.BillPredicateBuilder;
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

public class BillRepositoryImpl implements CustomRepository<BillDto, Bill> {

    @PersistenceContext
    private EntityManager manager;

    @Override
    public BillDto getSingle(UUID id) {
        var predicate = BillPredicateBuilder.id(id);
        return columnsQuery(predicate).fetchFirst();
    }

    @Override
    public JPAQuery<Object> mainQuery(Predicate predicate) {
        return new JPAQuery<>(manager)
                .from(QBill.bill)
                .innerJoin(QBill.bill.referenceDay)
                .innerJoin(QBill.bill.dueDate)
                .innerJoin(QBill.bill.billType)
                .innerJoin(QBill.bill.budget)
                .leftJoin(QBill.bill.documentDate)
                .leftJoin(QBill.bill.paymentDate)
                .leftJoin(QBill.bill.balance)
                .where(predicate);
    }

    @Override
    public QBean<BillDto> columns() {
        return Projections.bean(
                BillDto.class,
                QBill.bill.createdDate,
                QBill.bill.lastModifiedDate,
                QBill.bill.id,
                QBill.bill.name,
                QBill.bill.active,
                QBill.bill.referenceDay.id.as("referenceDayId"),
                QBill.bill.referenceDay.calendarDate.as("referenceDayCalendarDate"),
                QBill.bill.documentDate.id.as("documentDateId"),
                QBill.bill.documentDate.calendarDate.as("documentDateCalendarDate"),
                QBill.bill.dueDate.id.as("dueDateId"),
                QBill.bill.dueDate.calendarDate.as("dueDateCalendarDate"),
                QBill.bill.value,
                QBill.bill.paymentDate.id.as("paymentDateId"),
                QBill.bill.paymentDate.calendarDate.as("paymentDateCalendarDate"),
                QBill.bill.observation,
                QBill.bill.installment,
                QBill.bill.billType.id.as("billTypeId"),
                QBill.bill.billType.name.as("billTypeName"),
                QBill.bill.documentType,
                QBill.bill.budget.id.as("budgetId"),
                QBill.bill.budget.name.as("budgetName"),
                QBill.bill.referenceType,
                QBill.bill.closed,
                QBill.bill.balance.id.as("balanceId"),
                QBill.bill.balance.name.as("balanceName")
        );
    }

    @Override
    public EntityPathBase<Bill> entityPathBase() {
        return QBill.bill;
    }

    @Override
    public BigDecimal getSum(Predicate predicate) {
        return BigDecimal.ZERO;
    }
}
