package com.onurbcd.cli.factory;

import com.onurbcd.cli.enums.FlowField;
import com.onurbcd.cli.param.flow.*;
import com.onurbcd.cli.util.CollectionUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.shell.jline.tui.component.flow.ComponentFlow;

import java.util.List;

import static com.onurbcd.cli.util.FlowBuilderWrapper.init;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FlowFactory {

    public static FlowSupplier create(ComponentFlow.Builder builder, Paramable params) {
        return switch (params.getType()) {
            case CATEGORY -> createCategorySaveFlow(builder, (CategorySaveFlowParam) params);
            case BILL_TYPE -> createBillTypeSaveFlow(builder, (BillTypeSaveFlowParam) params);
            case INCOME_SOURCE -> createIncomeSourceSaveFlow(builder, (IncomeSourceSaveFlowParam) params);
            case SOURCE -> createSourceSaveFlow(builder, (SourceSaveFlowParam) params);
            case BALANCE -> createBalanceSaveFlow(builder, (BalanceSaveFlowParam) params);
            case SECRET -> createSecretSaveFlow(builder, (SecretSaveFlowParam) params);
            case BUDGET -> createBudgetSaveFlow(builder, (BudgetSaveFlowParam) params);
            case BILL_OPEN -> createBillOpenSaveFlow(builder, (BillOpenSaveFlowParam) params);
            case BILL_CLOSE -> createBillCloseSaveFlow(builder, (BillCloseSaveFlowParam) params);
            case BILL -> createBillSaveFlow(builder, (BillSaveFlowParam) params);
        };
    }

    private static FlowSupplier createCategorySaveFlow(ComponentFlow.Builder builder, CategorySaveFlowParam params) {
        return () -> init(builder)
                .input(FlowField.NAME, params.getName())
                .input(FlowField.DESCRIPTION, params.getDescription())
                .select(FlowField.PARENT_ID, params.getItems(), params.getParent())
                .execute();
    }

    private static FlowSupplier createBillTypeSaveFlow(ComponentFlow.Builder builder, BillTypeSaveFlowParam params) {
        return () -> init(builder)
                .input(FlowField.NAME, params.getName())
                .input(FlowField.PATH, params.getPath())
                .select(FlowField.CATEGORY_ID, params.getCategoryItems(), params.getCategory())
                .execute();
    }

    private static FlowSupplier createIncomeSourceSaveFlow(ComponentFlow.Builder builder, IncomeSourceSaveFlowParam params) {
        return () -> init(builder)
                .input(FlowField.NAME, params.getName())
                .execute();
    }

    private static FlowSupplier createSourceSaveFlow(ComponentFlow.Builder builder, SourceSaveFlowParam params) {
        return () -> init(builder)
                .input(FlowField.NAME, params.getName())
                .select(FlowField.INCOME_SOURCE_ID, params.getIncomeSourceItems(), params.getIncomeSource())
                .select(FlowField.SOURCE_TYPE, params.getSourceTypeItems(), params.getSourceType())
                .select(FlowField.CURRENCY_TYPE, params.getCurrencyTypeItems(), params.getCurrencyType())
                .input(FlowField.BALANCE, params.getBalance())
                .execute();
    }

    private static FlowSupplier createBalanceSaveFlow(ComponentFlow.Builder builder, BalanceSaveFlowParam params) {
        return () -> {
            var wrapper = init(builder)
                    .input(FlowField.DAY, params.getDay())
                    .select(FlowField.SOURCE, params.getSourceItems(), params.getSource())
                    .select(FlowField.CATEGORY, params.getCategoryItems(), params.getCategory())
                    .input(FlowField.AMOUNT, params.getAmount())
                    .input(FlowField.CODE, params.getCode())
                    .input(FlowField.DESCRIPTION, params.getDescription())
                    .select(FlowField.BALANCE_TYPE, params.getBalanceTypeItems(), params.getBalanceType())
                    .select(FlowField.PAYMENT_TYPE, params.getPaymentTypeItems(), params.getPaymentType());

            if (CollectionUtil.isNotEmpty(params.getLinkedDocuments())) {
                wrapper.multiSelect(FlowField.DOCUMENTS_IDS, params.getLinkedDocuments(), params.getLinkedDocumentsDefault());
            }

            return wrapper.multiSelect(FlowField.DOCUMENTS, params.getFilesNames()).execute();
        };
    }

    private static FlowSupplier createSecretSaveFlow(ComponentFlow.Builder builder, SecretSaveFlowParam params) {
        return () -> init(builder)
                .input(FlowField.NAME, params.getName())
                .input(FlowField.DESCRIPTION, params.getDescription())
                .input(FlowField.LINK, params.getLink())
                .input(FlowField.USERNAME, params.getUsername())
                .secretInput(FlowField.PASSWORD)
                .execute();
    }

    private static FlowSupplier createBudgetSaveFlow(ComponentFlow.Builder builder, BudgetSaveFlowParam params) {
        return () -> init(builder)
                .input(FlowField.NAME, params.getName())
                .input(FlowField.REF_YEAR, params.getRefYear())
                .input(FlowField.REF_MONTH, params.getRefMonth())
                .select(FlowField.BILL_TYPE_ID, params.getBillTypeItems(), params.getBillType())
                .input(FlowField.AMOUNT, params.getAmount())
                .confirm(FlowField.PAID, params.getPaid())
                .execute();
    }

    private static FlowSupplier createBillOpenSaveFlow(ComponentFlow.Builder builder, BillOpenSaveFlowParam params) {
        return () -> init(builder)
                .input(FlowField.REFERENCE_DAY)
                .input(FlowField.DOCUMENT_DATE)
                .input(FlowField.DUE_DATE)
                .input(FlowField.OBSERVATION)
                .input(FlowField.INSTALLMENT)
                .select(FlowField.DOCUMENT_TYPE, params.getDocumentTypeItems())
                .select(FlowField.BUDGET, params.getBudgetItems())
                .select(FlowField.REFERENCE_TYPE, params.getReferenceTypeItems())
                .select(FlowField.DOCUMENT, params.getFilesNames())
                .execute();
    }

    private static FlowSupplier createBillCloseSaveFlow(ComponentFlow.Builder builder, BillCloseSaveFlowParam params) {
        return () -> init(builder)
                .select(FlowField.BILL_ID, params.getBillItems())
                .input(FlowField.PAYMENT_DATE)
                .select(FlowField.PAYMENT_TYPE, params.getPaymentTypeItems())
                .select(FlowField.SOURCE_ID, params.getSourceItems())
                .select(FlowField.RECEIPT, params.getFilesNames())
                .execute();
    }

    private static FlowSupplier createBillSaveFlow(ComponentFlow.Builder builder, BillSaveFlowParam params) {
        return () -> {
            var wrapper = init(builder)
                    .input(FlowField.REFERENCE_DAY, params.getReferenceDay())
                    .input(FlowField.DOCUMENT_DATE, params.getDocumentDate())
                    .input(FlowField.DUE_DATE, params.getDueDate())
                    .input(FlowField.PAYMENT_DATE, params.getPaymentDate());

            if (params.getBillDocumentItem() != null) {
                wrapper.select(FlowField.LINKED_DOCUMENT, List.of(params.getBillDocumentItem()), params.getBillDocument());
            }

            if (params.getReceiptItem() != null) {
                wrapper.select(FlowField.LINKED_RECEIPT, List.of(params.getReceiptItem()), params.getReceipt());
            }

            return wrapper.input(FlowField.OBSERVATION, params.getObservation())
                    .input(FlowField.INSTALLMENT, params.getInstallment())
                    .select(FlowField.DOCUMENT_TYPE, params.getDocumentTypeItems(), params.getDocumentType())
                    .select(FlowField.BUDGET, params.getBudgetItems(), params.getBudget())
                    .select(FlowField.REFERENCE_TYPE, params.getReferenceTypeItems(), params.getReferenceType())
                    .select(FlowField.DOCUMENT, params.getFilesNames())
                    .select(FlowField.RECEIPT, params.getFilesNames())
                    .select(FlowField.PAYMENT_TYPE, params.getPaymentTypeItems(), params.getPaymentType())
                    .select(FlowField.SOURCE_ID, params.getSourceItems(), params.getSource())
                    .execute();
        };
    }
}
