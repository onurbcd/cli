package com.onurbcd.cli.model;

import com.onurbcd.cli.enums.FlowType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.springframework.shell.component.flow.SelectItem;

import java.util.List;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class SaveFlowParam {

    private FlowType type;
    private List<SelectItem> sourceItems;
    private List<SelectItem> categoryItems;
    private String filesPath;
    private List<SelectItem> billTypeItems;
    private List<SelectItem> incomeSourceItems;

    public static SaveFlowParam noArgs(FlowType type) {
        return SaveFlowParam.builder()
                .type(type)
                .build();
    }

    public static SaveFlowParam balance(List<SelectItem> sourceItems, List<SelectItem> categoryItems, String filesPath) {
        return SaveFlowParam.builder()
                .type(FlowType.BALANCE)
                .sourceItems(sourceItems)
                .categoryItems(categoryItems)
                .filesPath(filesPath)
                .build();
    }

    public static SaveFlowParam billType(List<SelectItem> categoryItems) {
        return SaveFlowParam.builder()
                .type(FlowType.BILL_TYPE)
                .categoryItems(categoryItems)
                .build();
    }

    public static SaveFlowParam budget(List<SelectItem> billTypeItems) {
        return SaveFlowParam.builder()
                .type(FlowType.BUDGET)
                .billTypeItems(billTypeItems)
                .build();
    }

    public static SaveFlowParam category(List<SelectItem> categoryItems) {
        return SaveFlowParam.builder()
                .type(FlowType.CATEGORY)
                .categoryItems(categoryItems)
                .build();
    }

    public static SaveFlowParam source(List<SelectItem> incomeSourceItems) {
        return SaveFlowParam.builder()
                .type(FlowType.SOURCE)
                .incomeSourceItems(incomeSourceItems)
                .build();
    }
}
