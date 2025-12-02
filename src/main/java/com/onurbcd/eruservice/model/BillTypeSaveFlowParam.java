package com.onurbcd.eruservice.model;

import java.util.List;
import java.util.Optional;

import org.springframework.shell.component.flow.SelectItem;
import org.springframework.shell.standard.ShellOption;

import com.onurbcd.eruservice.dto.billtype.BillTypeDto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BillTypeSaveFlowParam {

    private String name;
    private String path;
    private List<SelectItem> categoryItems;
    private String category;

    public static BillTypeSaveFlowParam of(BillTypeDto billType, List<SelectItem> categoryItems) {
        return BillTypeSaveFlowParam
                .builder()
                .name(Optional.ofNullable(billType).map(BillTypeDto::getName).orElse(ShellOption.NULL))
                .path(Optional.ofNullable(billType).map(BillTypeDto::getPath).orElse(ShellOption.NULL))
                .categoryItems(categoryItems)
                .category(Optional.ofNullable(billType).map(BillTypeDto::getCategoryName).orElse(null))
                .build();
    }
}
