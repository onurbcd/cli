package com.onurbcd.cli.model;

import com.onurbcd.cli.dto.category.CategoryDto;
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
public class CategorySaveFlowParam implements Paramable {

    private String name;
    private String description;
    private List<SelectItem> items;
    private String parent;

    public static CategorySaveFlowParam of(@Nullable CategoryDto category, SaveFlowParam params) {
        return CategorySaveFlowParam.builder()
                .name(getString(category, CategoryDto::getName))
                .description(getString(category, CategoryDto::getDescription))
                .items(params.getCategoryItems())
                .parent(getNullString(category, CategoryDto::getParentName))
                .build();
    }

    @Override
    public FlowType getType() {
        return FlowType.CATEGORY;
    }
}
