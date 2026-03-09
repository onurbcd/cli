package com.onurbcd.eruservice.model;

import com.onurbcd.eruservice.dto.billtype.BillTypeDto;
import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.flow.SelectItem;

import java.util.List;

import static com.onurbcd.eruservice.util.FlowParamUtil.getNullString;
import static com.onurbcd.eruservice.util.FlowParamUtil.getString;

@Builder
@Getter
public class BillTypeSaveFlowParam {

    private String name;
    private String path;
    private List<SelectItem> categoryItems;
    private String category;

    public static BillTypeSaveFlowParam of(@Nullable BillTypeDto billType, List<SelectItem> categoryItems) {
        return BillTypeSaveFlowParam.builder()
                .name(getString(billType, BillTypeDto::getName))
                .path(getString(billType, BillTypeDto::getPath))
                .categoryItems(categoryItems)
                .category(getNullString(billType, BillTypeDto::getCategoryName))
                .build();
    }
}
