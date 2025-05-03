package com.onurbcd.eruservice.dto.bill;

import com.onurbcd.eruservice.dto.PrimeSaveDto;
import com.onurbcd.eruservice.enums.DocumentType;
import com.onurbcd.eruservice.enums.PaymentType;
import com.onurbcd.eruservice.enums.ReferenceType;
import com.onurbcd.eruservice.util.Constant;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@SuperBuilder
@Getter
@Setter
public class BillSaveDto extends PrimeSaveDto {

    @NotNull
    private LocalDate referenceDayCalendarDate;

    private LocalDate documentDateCalendarDate;

    @NotNull
    private LocalDate dueDateCalendarDate;

    @NotNull
    @DecimalMin(Constant.POSITIVE_AMOUNT_MIN)
    @DecimalMax(Constant.AMOUNT_MAX)
    private BigDecimal value;

    private LocalDate paymentDateCalendarDate;

    private UUID billDocumentId;

    private UUID receiptId;

    @Size(max = 250)
    private String observation;

    @Min(1)
    private Short installment;

    @NotNull
    private UUID billTypeId;

    @NotNull
    private DocumentType documentType;

    private PaymentType paymentType;

    @NotNull
    private UUID budgetId;

    private UUID sourceId;

    @NotNull
    private ReferenceType referenceType;

    @NotNull
    private Boolean closed;

    private UUID balanceId;
}
