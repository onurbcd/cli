package com.onurbcd.eruservice.persistency.repository;

import com.onurbcd.eruservice.dto.Dtoable;
import com.onurbcd.eruservice.persistency.entity.Entityable;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface CustomRepository<D extends Dtoable, E extends Entityable> {

    default Page<D> getAll(Predicate predicate, Pageable pageable) {
        var content = columnsQuery(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orderBy(pageable.getSort(), entityPathBase().getMetadata().getName()))
                .fetch();

        final var totalSupplier = countTotalRows(content, pageable, predicate);
        return PageableExecutionUtils.getPage(content, pageable, () -> totalSupplier);
    }

    default List<D> getAll(Predicate predicate) {
        return columnsQuery(predicate).fetch();
    }

    @Nullable
    D getSingle(UUID id);

    JPAQuery<Object> mainQuery(Predicate predicate);

    default JPAQuery<D> columnsQuery(Predicate predicate) {
        return mainQuery(predicate).select(columns());
    }

    QBean<D> columns();

    EntityPathBase<E> entityPathBase();

    BigDecimal getSum(Predicate predicate);

    private Long countTotalRows(List<D> dtos, Pageable pageable, Predicate predicate) {
        return dtos.size() < pageable.getPageSize()
                ? dtos.size()
                : mainQuery(predicate).select(Projections.appending(entityPathBase().count())).fetchFirst();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private OrderSpecifier<?>[] orderBy(Sort sort, String alias) {
        var size = (int) sort.stream().count();
        var result = new OrderSpecifier[size];
        var index = 0;

        for (Sort.Order order : sort) {
            var pathBuilder = new PathBuilder<>(Object.class, alias);
            var target = pathBuilder.get(order.getProperty());
            result[index] = new OrderSpecifier(order.isAscending() ? Order.ASC : Order.DESC, target);
            index++;
        }

        return result;
    }
}
