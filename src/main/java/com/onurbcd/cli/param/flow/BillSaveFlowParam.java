package com.onurbcd.cli.param.flow;

import com.onurbcd.cli.dto.bill.BillDto;
import com.onurbcd.cli.enums.DocumentType;
import com.onurbcd.cli.enums.FlowType;
import com.onurbcd.cli.enums.PaymentType;
import com.onurbcd.cli.enums.ReferenceType;
import lombok.Builder;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.shell.jline.tui.component.flow.SelectItem;

import java.util.List;

import static com.onurbcd.cli.util.EnumUtil.getCodeableItems;
import static com.onurbcd.cli.util.FileUtil.getFiles;
import static com.onurbcd.cli.util.ParamUtil.*;

@Builder
@Getter
public class BillSaveFlowParam implements Paramable {

    private String referenceDay;
    private String documentDate;
    private String dueDate;
    private String paymentDate;
    private String billDocument;
    private SelectItem billDocumentItem;
    private String receipt;
    private SelectItem receiptItem;
    private String observation;
    private String installment;
    private String documentType;
    private List<SelectItem> documentTypeItems;
    private String budget;
    private List<SelectItem> budgetItems;
    private String referenceType;
    private List<SelectItem> referenceTypeItems;
    private List<SelectItem> filesNames;
    private String paymentType;
    private List<SelectItem> paymentTypeItems;
    private String source;
    private List<SelectItem> sourceItems;

    @Override
    public FlowType getType() {
        return FlowType.BILL;
    }

    public static BillSaveFlowParam of(@Nullable BillDto billDto, SaveFlowParam params) {
        return BillSaveFlowParam.builder()
                .referenceDay(getLocalDate(billDto, BillDto::getReferenceDayCalendarDate))
                .documentDate(getLocalDate(billDto, BillDto::getDocumentDateCalendarDate))
                .dueDate(getLocalDate(billDto, BillDto::getDueDateCalendarDate))
                .paymentDate(getLocalDate(billDto, BillDto::getPaymentDateCalendarDate))
                .billDocument(getUUID(billDto, BillDto::getBillDocumentId))
                .billDocumentItem(getDocItem(billDto, BillDto::getBillDocument))
                .receipt(getUUID(billDto, BillDto::getReceiptId))
                .receiptItem(getDocItem(billDto, BillDto::getReceipt))
                .observation(getString(billDto, BillDto::getObservation))
                .installment(getShort(billDto, BillDto::getInstallment))
                .documentType(getEnum(billDto, BillDto::getDocumentType))
                .documentTypeItems(getCodeableItems(DocumentType.values()))
                .budget(getUUID(billDto, BillDto::getBudgetId))
                .budgetItems(params.getBudgetItems())
                .referenceType(getEnum(billDto, BillDto::getReferenceType))
                .referenceTypeItems(getCodeableItems(ReferenceType.values()))
                .filesNames(getFiles(params.getFilesPath()))
                .paymentType(getEnum(billDto, BillDto::getPaymentType))
                .paymentTypeItems(getCodeableItems(PaymentType.values()))
                .source(getUUID(billDto, BillDto::getSourceId))
                .sourceItems(params.getSourceItems())
                .build();
    }
}
