package com.onurbcd.cli.persistency.repository.impl;

import com.onurbcd.cli.dto.billtype.BillTypeDto;
import com.onurbcd.cli.persistency.entity.BillType;
import com.onurbcd.cli.persistency.entity.QBillType;
import com.onurbcd.cli.persistency.predicate.BillTypePredicateBuilder;
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

public class BillTypeRepositoryImpl implements CustomRepository<BillTypeDto, BillType> {

    @PersistenceContext
    private EntityManager manager;

    @Override
    public BillTypeDto getSingle(UUID id) {
        var predicate = BillTypePredicateBuilder.id(id);
        return columnsQuery(predicate).fetchFirst();
    }

    @Override
    public JPAQuery<Object> mainQuery(Predicate predicate) {
        return new JPAQuery<>(manager)
                .from(QBillType.billType)
                .innerJoin(QBillType.billType.category)
                .where(predicate);
    }

    @Override
    public QBean<BillTypeDto> columns() {
        return Projections.bean(
                BillTypeDto.class,
                QBillType.billType.createdDate,
                QBillType.billType.lastModifiedDate,
                QBillType.billType.id,
                QBillType.billType.name,
                QBillType.billType.active,
                QBillType.billType.path,
                QBillType.billType.category.id.as("categoryId"),
                QBillType.billType.category.name.as("categoryName")
        );
    }

    @Override
    public EntityPathBase<BillType> entityPathBase() {
        return QBillType.billType;
    }

    @Override
    public BigDecimal getSum(Predicate predicate) {
        return BigDecimal.ZERO;
    }
}
