package com.onurbcd.eruservice.dto.filter;

import com.onurbcd.eruservice.enums.DocumentType;
import com.onurbcd.eruservice.dto.enums.PaymentType;
import com.onurbcd.eruservice.dto.enums.ReferenceType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.UUID;

@SuperBuilder
@Getter
@Setter
public class BillFilter extends AbstractFilterable {

    private Short referenceDayYear;

    private Short referenceDayMonth;

    private LocalDate documentDateCalendarDate;

    private LocalDate dueDateCalendarDate;

    private LocalDate paymentDateCalendarDate;

    private UUID billTypeId;

    private DocumentType documentType;

    private PaymentType paymentType;

    private UUID sourceId;

    private ReferenceType referenceType;

    private Boolean closed;
}
