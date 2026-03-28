package com.onurbcd.cli.param.flow;

import com.onurbcd.cli.dto.billtype.BillTypeDto;
import com.onurbcd.cli.enums.FlowType;
import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.flow.SelectItem;

import java.util.List;

import static com.onurbcd.cli.util.ParamUtil.getNullString;
import static com.onurbcd.cli.util.ParamUtil.getString;

@Builder
@Getter
public class BillTypeSaveFlowParam implements Paramable {

    private String name;
    private String path;
    private List<SelectItem> categoryItems;
    private String category;

    public static BillTypeSaveFlowParam of(@Nullable BillTypeDto billType, SaveFlowParam params) {
        return BillTypeSaveFlowParam.builder()
                .name(getString(billType, BillTypeDto::getName))
                .path(getString(billType, BillTypeDto::getPath))
                .categoryItems(params.getCategoryItems())
                .category(getNullString(billType, BillTypeDto::getCategoryName))
                .build();
    }

    @Override
    public FlowType getType() {
        return FlowType.BILL_TYPE;
    }
}
