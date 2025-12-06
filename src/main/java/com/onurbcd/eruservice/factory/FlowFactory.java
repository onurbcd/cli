package com.onurbcd.eruservice.factory;

import static com.onurbcd.eruservice.util.Constant.BALANCE;
import static com.onurbcd.eruservice.util.Constant.BALANCE_LABEL;
import static com.onurbcd.eruservice.util.Constant.CATEGORY_ID;
import static com.onurbcd.eruservice.util.Constant.CATEGORY_ID_LABEL;
import static com.onurbcd.eruservice.util.Constant.CURRENCY_TYPE;
import static com.onurbcd.eruservice.util.Constant.CURRENCY_TYPE_LABEL;
import static com.onurbcd.eruservice.util.Constant.DESCRIPTION;
import static com.onurbcd.eruservice.util.Constant.DESCRIPTION_LABEL;
import static com.onurbcd.eruservice.util.Constant.INCOME_SOURCE_ID;
import static com.onurbcd.eruservice.util.Constant.INCOME_SOURCE_ID_LABEL;
import static com.onurbcd.eruservice.util.Constant.NAME;
import static com.onurbcd.eruservice.util.Constant.NAME_LABEL;
import static com.onurbcd.eruservice.util.Constant.PARENT_ID;
import static com.onurbcd.eruservice.util.Constant.PARENT_ID_LABEL;
import static com.onurbcd.eruservice.util.Constant.PATH;
import static com.onurbcd.eruservice.util.Constant.PATH_LABEL;
import static com.onurbcd.eruservice.util.Constant.SOURCE_TYPE;
import static com.onurbcd.eruservice.util.Constant.SOURCE_TYPE_LABEL;

import java.util.function.Supplier;

import org.springframework.shell.component.flow.ComponentFlow;

import com.onurbcd.eruservice.model.BillTypeSaveFlowParam;
import com.onurbcd.eruservice.model.CategorySaveFlowParam;
import com.onurbcd.eruservice.model.IncomeSourceSaveFlowParam;
import com.onurbcd.eruservice.model.SourceSaveFlowParam;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FlowFactory {

    public static final Supplier<ComponentFlow.ComponentFlowResult> createCategorySaveFlow(
            ComponentFlow.Builder flowBuilder, CategorySaveFlowParam params) {

        return () -> {
            return flowBuilder.clone().reset()
                    .withStringInput(NAME).name(NAME_LABEL).defaultValue(params.getName()).and()
                    .withStringInput(DESCRIPTION).name(DESCRIPTION_LABEL).defaultValue(params.getDescription()).and()
                    .withSingleItemSelector(PARENT_ID).name(PARENT_ID_LABEL).selectItems(params.getItems())
                    .defaultSelect(params.getParent()).max(params.getItems().size()).and()
                    .build().run();
        };
    }

    public static final Supplier<ComponentFlow.ComponentFlowResult> createBillTypeSaveFlow(
            ComponentFlow.Builder flowBuilder, BillTypeSaveFlowParam params) {

        return () -> {
            return flowBuilder.clone().reset()
                    .withStringInput(NAME).name(NAME_LABEL).defaultValue(params.getName()).and()
                    .withStringInput(PATH).name(PATH_LABEL).defaultValue(params.getPath()).and()
                    .withSingleItemSelector(CATEGORY_ID).name(CATEGORY_ID_LABEL).selectItems(params.getCategoryItems())
                    .defaultSelect(params.getCategory()).max(params.getCategoryItems().size()).and()
                    .build().run();
        };
    }

    public static final Supplier<ComponentFlow.ComponentFlowResult> createIncomeSourceSaveFlow(
            ComponentFlow.Builder flowBuilder, IncomeSourceSaveFlowParam params) {

        return () -> {
            return flowBuilder.clone().reset()
                    .withStringInput(NAME).name(NAME_LABEL).defaultValue(params.getName()).and()
                    .build().run();
        };
    }

    public static final Supplier<ComponentFlow.ComponentFlowResult> createSourceSaveFlow(
            ComponentFlow.Builder flowBuilder, SourceSaveFlowParam params) {

        return () -> {
            return flowBuilder.clone().reset()
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
        };
    }
}
