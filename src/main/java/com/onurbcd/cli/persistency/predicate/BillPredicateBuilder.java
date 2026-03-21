package com.onurbcd.cli.persistency.predicate;

import com.onurbcd.cli.dto.filter.BillFilter;
import com.onurbcd.cli.enums.DocumentType;
import com.onurbcd.cli.enums.ReferenceType;
import com.onurbcd.cli.persistency.entity.QBill;
import com.querydsl.core.types.Predicate;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.util.UUID;

public class BillPredicateBuilder extends BasePredicateBuilder {

    public BillPredicateBuilder() {
        super(QBill.bill.observation, QBill.bill.active);
    }

    public static Predicate id(UUID id) {
        return new BillPredicateBuilder().idEq(id).build();
    }

    public static Predicate all(BillFilter filter) {
        return new BillPredicateBuilder()
                .referenceDayYear(filter.getReferenceDayYear())
                .referenceDayMonth(filter.getReferenceDayMonth())
                .documentDateCalendarDate(filter.getDocumentDateCalendarDate())
                .dueDateCalendarDate(filter.getDueDateCalendarDate())
                .paymentDateCalendarDate(filter.getPaymentDateCalendarDate())
                .billTypeId(filter.getBillTypeId())
                .documentType(filter.getDocumentType())
                .referenceType(filter.getReferenceType())
                .closed(filter.getClosed())
                .search(filter.getSearch())
                .active(filter.isActive())
                .build();
    }

    private BillPredicateBuilder idEq(UUID id) {
        builder().and(QBill.bill.id.eq(id));
        return this;
    }

    private BillPredicateBuilder referenceDayYear(@Nullable Short referenceDayYear) {
        if (referenceDayYear != null) {
            builder().and(QBill.bill.referenceDay.calendarYear.eq(referenceDayYear));
        }

        return this;
    }

    private BillPredicateBuilder referenceDayMonth(@Nullable Short referenceDayMonth) {
        if (referenceDayMonth != null) {
            builder().and(QBill.bill.referenceDay.calendarMonth.eq(referenceDayMonth));
        }

        return this;
    }

    private BillPredicateBuilder documentDateCalendarDate(@Nullable LocalDate documentDateCalendarDate) {
        if (documentDateCalendarDate != null) {
            builder().and(QBill.bill.documentDate.calendarDate.eq(documentDateCalendarDate));
        }

        return this;
    }

    private BillPredicateBuilder dueDateCalendarDate(@Nullable LocalDate dueDateCalendarDate) {
        if (dueDateCalendarDate != null) {
            builder().and(QBill.bill.dueDate.calendarDate.eq(dueDateCalendarDate));
        }

        return this;
    }

    private BillPredicateBuilder paymentDateCalendarDate(@Nullable LocalDate paymentDateCalendarDate) {
        if (paymentDateCalendarDate != null) {
            builder().and(QBill.bill.paymentDate.calendarDate.eq(paymentDateCalendarDate));
        }

        return this;
    }

    private BillPredicateBuilder billTypeId(@Nullable UUID billTypeId) {
        if (billTypeId != null) {
            builder().and(QBill.bill.billType.id.eq(billTypeId));
        }

        return this;
    }

    private BillPredicateBuilder documentType(@Nullable DocumentType documentType) {
        if (documentType != null) {
            builder().and(QBill.bill.documentType.eq(documentType));
        }

        return this;
    }

    private BillPredicateBuilder referenceType(@Nullable ReferenceType referenceType) {
        if (referenceType != null) {
            builder().and(QBill.bill.referenceType.eq(referenceType));
        }

        return this;
    }

    private BillPredicateBuilder closed(@Nullable Boolean closed) {
        if (closed != null) {
            builder().and(QBill.bill.closed.eq(closed));
        }

        return this;
    }
}
