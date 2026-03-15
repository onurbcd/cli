package com.onurbcd.cli.persistency.predicate;

import com.onurbcd.cli.enums.BalanceType;
import com.onurbcd.cli.dto.filter.BalanceFilter;
import com.onurbcd.cli.persistency.entity.QBalance;
import com.querydsl.core.types.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.util.UUID;

public class BalancePredicateBuilder extends BasePredicateBuilder {

    public BalancePredicateBuilder() {
        super(QBalance.balance.name, QBalance.balance.active);
    }

    public static Predicate id(UUID id) {
        return new BalancePredicateBuilder().idEq(id).build();
    }

    public static Predicate all(BalanceFilter filter) {
        return new BalancePredicateBuilder()
                .dayCalendarDate(filter.getDayCalendarDate())
                .dayCalendarYear(filter.getDayCalendarYear())
                .dayCalendarMonth(filter.getDayCalendarMonth())
                .dayCalendarDayInMonth(filter.getDayCalendarDayInMonth())
                .sourceId(filter.getSourceId())
                .categoryId(filter.getCategoryId())
                .balanceType(filter.getBalanceType())
                .search(filter.getSearch())
                .active(filter.isActive())
                .build();
    }

    @Override
    public BalancePredicateBuilder search(String search) {
        if (StringUtils.isNotBlank(search)) {
            builder().and(QBalance.balance.code.containsIgnoreCase(search)
                    .or(QBalance.balance.description.containsIgnoreCase(search)));
        }

        return this;
    }

    private BalancePredicateBuilder idEq(UUID id) {
        builder().and(QBalance.balance.id.eq(id));
        return this;
    }

    private BalancePredicateBuilder dayCalendarDate(@Nullable LocalDate dayCalendarDate) {
        if (dayCalendarDate != null) {
            builder().and(QBalance.balance.day.calendarDate.eq(dayCalendarDate));
        }

        return this;
    }

    private BalancePredicateBuilder sourceId(@Nullable UUID sourceId) {
        if (sourceId != null) {
            builder().and(QBalance.balance.source.id.eq(sourceId));
        }

        return this;
    }

    private BalancePredicateBuilder categoryId(@Nullable UUID categoryId) {
        if (categoryId != null) {
            builder().and(QBalance.balance.category.id.eq(categoryId));
        }

        return this;
    }

    private BalancePredicateBuilder balanceType(@Nullable BalanceType balanceType) {
        if (balanceType != null) {
            builder().and(QBalance.balance.balanceType.eq(balanceType));
        }

        return this;
    }

    private BalancePredicateBuilder dayCalendarYear(@Nullable Short dayCalendarYear) {
        if (dayCalendarYear != null) {
            builder().and(QBalance.balance.day.calendarYear.eq(dayCalendarYear));
        }

        return this;
    }

    private BalancePredicateBuilder dayCalendarMonth(@Nullable Short dayCalendarMonth) {
        if (dayCalendarMonth != null) {
            builder().and(QBalance.balance.day.calendarMonth.eq(dayCalendarMonth));
        }

        return this;
    }

    private BalancePredicateBuilder dayCalendarDayInMonth(@Nullable Short dayCalendarDayInMonth) {
        if (dayCalendarDayInMonth != null) {
            builder().and(QBalance.balance.day.calendarDayInMonth.eq(dayCalendarDayInMonth));
        }

        return this;
    }
}
