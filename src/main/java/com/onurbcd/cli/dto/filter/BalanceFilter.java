package com.onurbcd.cli.dto.filter;

import com.onurbcd.cli.enums.BalanceType;
import com.onurbcd.cli.enums.PaymentType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.UUID;

import static com.onurbcd.cli.util.DateUtil.orCurrentMonth;
import static com.onurbcd.cli.util.DateUtil.orCurrentYear;

@SuperBuilder
@Getter
@Setter
public class BalanceFilter extends AbstractFilterable {

    private LocalDate dayCalendarDate;
    private UUID sourceId;
    private UUID categoryId;
    private BalanceType balanceType;
    private PaymentType paymentType;
    private Short dayCalendarYear;
    private Short dayCalendarMonth;
    private Short dayCalendarDayInMonth;

    public static BalanceFilter of(Boolean active, String search, UUID sourceId, UUID categoryId,
                                   BalanceType balanceType, PaymentType paymentType) {

        return BalanceFilter
                .builder()
                .active(active)
                .search(search)
                .sourceId(sourceId)
                .categoryId(categoryId)
                .balanceType(balanceType)
                .paymentType(paymentType)
                .build();
    }

    public BalanceFilter and(Short year, Short month, Short day) {
        this.dayCalendarYear = orCurrentYear(year);
        this.dayCalendarMonth = orCurrentMonth(month);
        this.dayCalendarDayInMonth = day;
        return this;
    }
}
