package com.onurbcd.cli.dto.bill;

import com.onurbcd.cli.dto.PrimeSaveDto;
import com.onurbcd.cli.enums.DocumentType;
import com.onurbcd.cli.enums.ReferenceType;
import com.onurbcd.cli.param.MultipartFile;
import com.onurbcd.cli.util.Constant;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.shell.component.context.ComponentContext;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.UUID;

import static com.onurbcd.cli.util.Constant.*;
import static com.onurbcd.cli.util.Converter.toUUID;
import static com.onurbcd.cli.util.DateUtil.parseLocalDate;
import static com.onurbcd.cli.util.EnumUtil.valueOf;
import static com.onurbcd.cli.util.FileUtil.fileToMultipartFile;
import static com.onurbcd.cli.util.FlowUtil.getString;
import static com.onurbcd.cli.util.NumberUtil.parseShort;
import static com.onurbcd.cli.util.StringUtil.normalizeSpace;

@SuperBuilder
@Getter
@Setter
@Validated
public class BillOpenDto extends PrimeSaveDto {

    @NotNull(message = "Reference day is required.")
    private LocalDate referenceDayCalendarDate;

    private LocalDate documentDateCalendarDate;

    @NotNull(message = "Due date is required.")
    private LocalDate dueDateCalendarDate;

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

    private MultipartFile multipartFile;

    public static BillOpenDto of(ComponentContext<?> context) {
        return BillOpenDto.builder()
                .name(Constant.BOGUS_NAME)
                .active(Boolean.TRUE)
                .referenceDayCalendarDate(parseLocalDate(getString(context, REFERENCE_DAY_ID)))
                .documentDateCalendarDate(parseLocalDate(getString(context, DOCUMENT_DATE_ID)))
                .dueDateCalendarDate(parseLocalDate(getString(context, DUE_DATE_ID)))
                .observation(normalizeSpace(getString(context, OBSERVATION_ID)))
                .installment(parseShort(getString(context, INSTALLMENT_ID)))
                .documentType(valueOf(DocumentType.class, getString(context, DOCUMENT_TYPE_ID)))
                .budgetId(toUUID(getString(context, BUDGET_ID)))
                .referenceType(valueOf(ReferenceType.class, getString(context, REFERENCE_TYPE_ID)))
                .multipartFile(fileToMultipartFile(getString(context, DOCUMENT_ID)))
                .build();
    }
}
