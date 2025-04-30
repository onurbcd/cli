package com.onurbcd.eruservice.dto.bill;

import com.onurbcd.eruservice.dto.PrimeDto;
import com.onurbcd.eruservice.dto.document.DocumentDto;
import com.onurbcd.eruservice.enums.DocumentType;
import com.onurbcd.eruservice.dto.enums.PaymentType;
import com.onurbcd.eruservice.dto.enums.ReferenceType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class BillDto extends PrimeDto {

    private Integer referenceDayId;

    private LocalDate referenceDayCalendarDate;

    private Integer documentDateId;

    private LocalDate documentDateCalendarDate;

    private Integer dueDateId;

    private LocalDate dueDateCalendarDate;

    private BigDecimal value;

    private Integer paymentDateId;

    private LocalDate paymentDateCalendarDate;

    private DocumentDto billDocument;

    private DocumentDto receipt;

    private String observation;

    private Short installment;

    private UUID billTypeId;

    private String billTypeName;

    private DocumentType documentType;

    private PaymentType paymentType;

    private UUID budgetId;

    private String budgetName;

    private UUID sourceId;

    private String sourceName;

    private ReferenceType referenceType;

    private Boolean closed;

    private UUID balanceId;

    private String balanceName;
}
