package com.onurbcd.cli.factory;

import com.onurbcd.cli.enums.FlowField;
import com.onurbcd.cli.model.*;
import com.onurbcd.cli.util.CollectionUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.shell.component.flow.ComponentFlow;

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
                .input(FlowField.PASSWORD, params.getPassword())
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
}
