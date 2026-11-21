package com.onurbcd.cli.dto.bill;

import com.onurbcd.cli.dto.PrimeSaveDto;
import com.onurbcd.cli.enums.DocumentType;
import com.onurbcd.cli.enums.PaymentType;
import com.onurbcd.cli.enums.ReferenceType;
import com.onurbcd.cli.param.MultipartFile;
import com.onurbcd.cli.util.EnumUtil;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.Nullable;
import org.springframework.shell.jline.tui.component.context.ComponentContext;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Function;

import static com.onurbcd.cli.util.Constant.*;
import static com.onurbcd.cli.util.Converter.toUUID;
import static com.onurbcd.cli.util.DateUtil.parseLocalDate;
import static com.onurbcd.cli.util.FileUtil.fileToMultipartFile;
import static com.onurbcd.cli.util.FlowUtil.getString;
import static com.onurbcd.cli.util.NumberUtil.parseShort;
import static com.onurbcd.cli.util.ParamUtil.getBoolean;
import static com.onurbcd.cli.util.ParamUtil.getNullUUID;
import static com.onurbcd.cli.util.StringUtil.normalizeSpace;

@SuperBuilder
@Getter
@Setter
@Validated
public class BillSaveDto extends PrimeSaveDto {

    @NotNull(message = "Reference day is required.")
    private LocalDate referenceDayCalendarDate;

    private LocalDate documentDateCalendarDate;

    @NotNull(message = "Due date is required.")
    private LocalDate dueDateCalendarDate;

    private LocalDate paymentDateCalendarDate;

    private UUID billDocumentId;
    private MultipartFile billDocumentFile;

    private UUID receiptId;
    private MultipartFile receiptFile;

    @Size(max = 250, message = "Observation must be less than {max} characters.")
    private String observation;

    @Min(value = 1, message = "Installment must be greater than or equal to {value}.")
    private Short installment;

    @NotNull(message = "Document type is required.")
    private DocumentType documentType;

    @NotNull(message = "Budget is required.")
    private UUID budgetId;

    @NotNull(message = "Reference type is required.")
    private ReferenceType referenceType;

    @NotNull(message = "Closed is required.")
    private Boolean closed;

    private UUID balanceId; // O que vai ser feito com o balance que já existe?

    @NotNull(message = "Payment type is required.")
    private PaymentType paymentType;

    @NotNull(message = "Source id is required.")
    private UUID sourceId;

    public static BillSaveDto of(ComponentContext<?> context, @Nullable BillDto billDto) {
        return BillSaveDto.builder()
                .name(BOGUS_NAME)
                .active(getBoolean(billDto, BillDto::getActive))
                .referenceDayCalendarDate(parseLocalDate(getString(context, REFERENCE_DAY_ID)))
                .documentDateCalendarDate(parseLocalDate(getString(context, DOCUMENT_DATE_ID)))
                .dueDateCalendarDate(parseLocalDate(getString(context, DUE_DATE_ID)))
                .paymentDateCalendarDate(parseLocalDate(getString(context, PAYMENT_DATE_ID)))
                .billDocumentId(getDocId(context, billDto, LINKED_DOCUMENT_ID, BillDto::getBillDocumentId))
                .billDocumentFile(fileToMultipartFile(getString(context, DOCUMENT_ID)))
                .receiptId(getDocId(context, billDto, LINKED_RECEIPT_ID, BillDto::getReceiptId))
                .receiptFile(fileToMultipartFile(getString(context, RECEIPT_ID)))
                .observation(normalizeSpace(getString(context, OBSERVATION_ID)))
                .installment(parseShort(getString(context, INSTALLMENT_ID)))
                .documentType(EnumUtil.valueOf(DocumentType.class, getString(context, DOCUMENT_TYPE_ID)))
                .budgetId(toUUID(getString(context, BUDGET_ID)))
                .referenceType(EnumUtil.valueOf(ReferenceType.class, getString(context, REFERENCE_TYPE_ID)))
                .closed(getBoolean(billDto, BillDto::getClosed))
                .paymentType(EnumUtil.valueOf(PaymentType.class, getString(context, PAYMENT_TYPE_ID)))
                .sourceId(toUUID(getString(context, SOURCE_ID_ID)))
                .build();
    }

    @Nullable
    private static UUID getDocId(ComponentContext<?> context, @Nullable BillDto billDto, String id,
                                 Function<BillDto, UUID> fn) {

        var linkedDocId = getString(context, id);

        return linkedDocId != null
                ? toUUID(linkedDocId)
                : getNullUUID(billDto, fn);
    }
}
