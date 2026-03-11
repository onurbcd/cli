package com.onurbcd.eruservice.factory;

import com.onurbcd.eruservice.enums.FlowField;
import com.onurbcd.eruservice.model.*;
import com.onurbcd.eruservice.util.CollectionUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.shell.component.flow.ComponentFlow;

import static com.onurbcd.eruservice.util.FlowBuilderWrapper.init;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FlowFactory {

    public static FlowSupplier createCategorySaveFlow(ComponentFlow.Builder builder, CategorySaveFlowParam params) {
        return () -> init(builder)
                .input(FlowField.NAME, params.getName())
                .input(FlowField.DESCRIPTION, params.getDescription())
                .select(FlowField.PARENT_ID, params.getItems(), params.getParent())
                .execute();
    }

    public static FlowSupplier createBillTypeSaveFlow(ComponentFlow.Builder builder, BillTypeSaveFlowParam params) {
        return () -> init(builder)
                .input(FlowField.NAME, params.getName())
                .input(FlowField.PATH, params.getPath())
                .select(FlowField.CATEGORY_ID, params.getCategoryItems(), params.getCategory())
                .execute();
    }

    public static FlowSupplier createIncomeSourceSaveFlow(ComponentFlow.Builder builder, IncomeSourceSaveFlowParam params) {
        return () -> init(builder)
                .input(FlowField.NAME, params.getName())
                .execute();
    }

    public static FlowSupplier createSourceSaveFlow(ComponentFlow.Builder builder, SourceSaveFlowParam params) {
        return () -> init(builder)
                .input(FlowField.NAME, params.getName())
                .select(FlowField.INCOME_SOURCE_ID, params.getIncomeSourceItems(), params.getIncomeSource())
                .select(FlowField.SOURCE_TYPE, params.getSourceTypeItems(), params.getSourceType())
                .select(FlowField.CURRENCY_TYPE, params.getCurrencyTypeItems(), params.getCurrencyType())
                .input(FlowField.BALANCE, params.getBalance())
                .execute();
    }

    public static FlowSupplier createBalanceSaveFlow(ComponentFlow.Builder builder, BalanceSaveFlowParam params) {
        return () -> {
            var wrapper = init(builder)
                    .input(FlowField.DAY, params.getDay())
                    .select(FlowField.SOURCE, params.getSourceItems(), params.getSource())
                    .select(FlowField.CATEGORY, params.getCategoryItems(), params.getCategory())
                    .input(FlowField.AMOUNT, params.getAmount())
                    .input(FlowField.CODE, params.getCode())
                    .input(FlowField.DESCRIPTION, params.getDescription())
                    .select(FlowField.BALANCE_TYPE, params.getBalanceTypeItems(), params.getBalanceType());

            if (CollectionUtil.isNotEmpty(params.getLinkedDocuments())) {
                wrapper.multiSelect(FlowField.DOCUMENTS_IDS, params.getLinkedDocuments(), params.getLinkedDocumentsDefault());
            }

            return wrapper.multiSelect(FlowField.DOCUMENTS, params.getFilesNames()).execute();
        };
    }
}
