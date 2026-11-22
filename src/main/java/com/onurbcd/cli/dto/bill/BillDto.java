package com.onurbcd.cli.dto.bill;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.onurbcd.cli.dto.PrimeDto;
import com.onurbcd.cli.dto.document.DocumentDto;
import com.onurbcd.cli.enums.DocumentType;
import com.onurbcd.cli.enums.PaymentType;
import com.onurbcd.cli.enums.ReferenceType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
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
    private UUID budgetId;
    private String budgetName;
    private ReferenceType referenceType;
    private Boolean closed;
    private UUID balanceId;
    private String balanceName;
    private PaymentType paymentType;
    private UUID sourceId;

    @Nullable
    public UUID getBillDocumentId() {
        return Optional.ofNullable(billDocument)
                .map(DocumentDto::getId)
                .orElse(null);
    }

    @Nullable
    public UUID getReceiptId() {
        return Optional.ofNullable(receipt)
                .map(DocumentDto::getId)
                .orElse(null);
    }
}
