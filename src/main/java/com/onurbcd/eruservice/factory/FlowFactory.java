package com.onurbcd.eruservice.factory;

import static com.onurbcd.eruservice.util.Constant.DESCRIPTION;
import static com.onurbcd.eruservice.util.Constant.DESCRIPTION_LABEL;
import static com.onurbcd.eruservice.util.Constant.NAME;
import static com.onurbcd.eruservice.util.Constant.NAME_LABEL;
import static com.onurbcd.eruservice.util.Constant.PARENT_ID;
import static com.onurbcd.eruservice.util.Constant.PARENT_ID_LABEL;

import java.util.function.Supplier;

import org.springframework.shell.component.flow.ComponentFlow;

import com.onurbcd.eruservice.model.CategorySaveFlowParam;

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
}
