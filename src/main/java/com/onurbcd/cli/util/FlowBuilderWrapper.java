package com.onurbcd.cli.util;

import com.onurbcd.cli.enums.FlowField;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.component.flow.ComponentFlow;
import org.springframework.shell.component.flow.SelectItem;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FlowBuilderWrapper {

    private final ComponentFlow.Builder builder;

    public static FlowBuilderWrapper init(ComponentFlow.Builder builder) {
        return new FlowBuilderWrapper(builder.clone().reset());
    }

    public FlowBuilderWrapper input(FlowField field, String defaultValue) {
        builder.withStringInput(field.getId())
                .name(field.getName())
                .defaultValue(defaultValue)
                .and();

        return this;
    }

    public FlowBuilderWrapper select(FlowField field, List<SelectItem> items, String defaultValue) {
        builder.withSingleItemSelector(field.getId())
                .name(field.getName())
                .selectItems(items)
                .defaultSelect(defaultValue)
                .max(items.size())
                .and();

        return this;
    }

    public FlowBuilderWrapper multiSelect(FlowField field, List<SelectItem> items) {
        return multiSelect(field, items, new ArrayList<>());
    }

    public FlowBuilderWrapper multiSelect(FlowField field, List<SelectItem> items, List<String> defaultValues) {
        builder.withMultiItemSelector(field.getId())
                .name(field.getName())
                .selectItems(items)
                .resultValues(defaultValues)
                .max(items.size())
                .and();

        return this;
    }

    public FlowBuilderWrapper confirm(FlowField field, Boolean defaultValue) {
        builder.withConfirmationInput(field.getId())
                .name(field.getName())
                .defaultValue(defaultValue)
                .and();

        return this;
    }

    public ComponentFlow.ComponentFlowResult execute() {
        return builder.build().run();
    }
}
