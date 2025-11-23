package com.onurbcd.eruservice.dto.bill;

import java.time.LocalDate;
import java.util.UUID;

import com.onurbcd.eruservice.dto.PrimeSaveDto;
import com.onurbcd.eruservice.enums.DocumentType;
import com.onurbcd.eruservice.enums.ReferenceType;
import com.onurbcd.eruservice.util.Constant;
import com.onurbcd.eruservice.util.Converter;
import com.onurbcd.eruservice.util.DateUtil;
import com.onurbcd.eruservice.util.EnumUtil;
import com.onurbcd.eruservice.util.Extension;
import com.onurbcd.eruservice.util.NumberUtil;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
@ExtensionMethod({ Extension.class })
public class BillOpenDto extends PrimeSaveDto {

    private LocalDate referenceDayCalendarDate;
    private LocalDate documentDateCalendarDate;
    private LocalDate dueDateCalendarDate;
    private String observation;
    private Short installment;
    private DocumentType documentType;
    private UUID budgetId;
    private ReferenceType referenceType;
    private String fileName;

    public static BillOpenDto of(String referenceDay, String documentDate, String dueDate, String observation,
            String installment, String documentType, String budgetId, String referenceType, String fileName) {

        return BillOpenDto
                .builder()
                .name(Constant.BOGUS_NAME)
                .active(Boolean.TRUE)
                .referenceDayCalendarDate(DateUtil.parseLocalDate(referenceDay))
                .documentDateCalendarDate(DateUtil.parseLocalDate(documentDate))
                .dueDateCalendarDate(DateUtil.parseLocalDate(dueDate))
                .observation(observation.defaultToNull())
                .installment(NumberUtil.parseShort(installment))
                .documentType(EnumUtil.valueOf(DocumentType.class, documentType))
                .budgetId(Converter.toUUID(budgetId))
                .referenceType(EnumUtil.valueOf(ReferenceType.class, referenceType))
                .fileName(fileName)
                .build();
    }
}
