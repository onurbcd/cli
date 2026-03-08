package com.onurbcd.eruservice.factory;

import com.onurbcd.eruservice.model.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.shell.component.flow.ComponentFlow;

import java.util.function.Supplier;

import static com.onurbcd.eruservice.util.Constant.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FlowFactory {

    public static Supplier<ComponentFlow.ComponentFlowResult> createCategorySaveFlow(
            ComponentFlow.Builder flowBuilder, CategorySaveFlowParam params) {

        return () -> flowBuilder.clone().reset()
                .withStringInput(NAME).name(NAME_LABEL).defaultValue(params.getName()).and()
                .withStringInput(DESCRIPTION).name(DESCRIPTION_LABEL).defaultValue(params.getDescription()).and()
                .withSingleItemSelector(PARENT_ID).name(PARENT_ID_LABEL).selectItems(params.getItems())
                .defaultSelect(params.getParent()).max(params.getItems().size()).and()
                .build().run();
    }

    public static Supplier<ComponentFlow.ComponentFlowResult> createBillTypeSaveFlow(
            ComponentFlow.Builder flowBuilder, BillTypeSaveFlowParam params) {

        return () -> flowBuilder.clone().reset()
                .withStringInput(NAME).name(NAME_LABEL).defaultValue(params.getName()).and()
                .withStringInput(PATH).name(PATH_LABEL).defaultValue(params.getPath()).and()
                .withSingleItemSelector(CATEGORY_ID).name(CATEGORY_ID_LABEL).selectItems(params.getCategoryItems())
                .defaultSelect(params.getCategory()).max(params.getCategoryItems().size()).and()
                .build().run();
    }

    public static Supplier<ComponentFlow.ComponentFlowResult> createIncomeSourceSaveFlow(
            ComponentFlow.Builder flowBuilder, IncomeSourceSaveFlowParam params) {

        return () -> flowBuilder.clone().reset()
                .withStringInput(NAME).name(NAME_LABEL).defaultValue(params.getName()).and()
                .build().run();
    }

    public static Supplier<ComponentFlow.ComponentFlowResult> createSourceSaveFlow(
            ComponentFlow.Builder flowBuilder, SourceSaveFlowParam params) {

        return () -> flowBuilder.clone().reset()
                .withStringInput(NAME).name(NAME_LABEL).defaultValue(params.getName()).and()
                .withSingleItemSelector(INCOME_SOURCE_ID).name(INCOME_SOURCE_ID_LABEL)
                .selectItems(params.getIncomeSourceItems()).defaultSelect(params.getIncomeSource())
                .max(params.getIncomeSourceItems().size()).and()
                .withSingleItemSelector(SOURCE_TYPE).name(SOURCE_TYPE_LABEL)
                .selectItems(params.getSourceTypeItems()).defaultSelect(params.getSourceType())
                .max(params.getSourceTypeItems().size()).and()
                .withSingleItemSelector(CURRENCY_TYPE).name(CURRENCY_TYPE_LABEL)
                .selectItems(params.getCurrencyTypeItems()).defaultSelect(params.getCurrencyType())
                .max(params.getCurrencyTypeItems().size()).and()
                .withStringInput(BALANCE).name(BALANCE_LABEL).defaultValue(params.getBalance()).and()
                .build().run();
    }

    public static Supplier<ComponentFlow.ComponentFlowResult> createBalanceSaveFlow(
            ComponentFlow.Builder flowBuilder, BalanceSaveFlowParam params) {

        return () -> {
            var builder = flowBuilder.clone().reset()
                    .withStringInput(DAY).name(DAY_LABEL).defaultValue(params.getDay()).and()
                    .withSingleItemSelector(SOURCE).name(SOURCE_LABEL).selectItems(params.getSourceItems()).defaultSelect(params.getSource()).max(params.getSourceItems().size()).and()
                    .withSingleItemSelector(CATEGORY).name(CATEGORY_LABEL).selectItems(params.getCategoryItems()).defaultSelect(params.getCategory()).max(params.getCategoryItems().size()).and()
                    .withStringInput(AMOUNT).name(AMOUNT_LABEL).defaultValue(params.getAmount()).and()
                    .withStringInput(CODE).name(CODE_LABEL).defaultValue(params.getCode()).and()
                    .withStringInput(DESCRIPTION).name(DESCRIPTION_LABEL).defaultValue(params.getDescription()).and()
                    .withSingleItemSelector(BALANCE_TYPE).name(BALANCE_TYPE_LABEL).selectItems(params.getBalanceTypeItems()).defaultSelect(params.getBalanceType()).max(params.getBalanceTypeItems().size()).and();

            if (params.getLinkedDocuments() != null && !params.getLinkedDocuments().isEmpty()) {
                builder = builder.withMultiItemSelector(DOCUMENTS_IDS).name(DOCUMENTS_IDS_LABEL)
                        .selectItems(params.getLinkedDocuments())
                        .resultValues(params.getLinkedDocumentsDefault())
                        .max(params.getLinkedDocuments().size()).and();
            }

            return builder
                    .withMultiItemSelector(DOCUMENTS).name(DOCUMENTS_LABEL).selectItems(params.getFilesNames())
                    .max(params.getFilesNames().size()).and()
                    .build().run();
        };
    }
}
