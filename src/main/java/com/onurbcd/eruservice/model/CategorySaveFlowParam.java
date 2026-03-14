package com.onurbcd.eruservice.model;

import com.onurbcd.eruservice.dto.category.CategoryDto;
import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.flow.SelectItem;

import java.util.List;

import static com.onurbcd.eruservice.util.ParamUtil.getNullString;
import static com.onurbcd.eruservice.util.ParamUtil.getString;

@Builder
@Getter
public class CategorySaveFlowParam {

    private String name;
    private String description;
    private List<SelectItem> items;
    private String parent;

    public static CategorySaveFlowParam of(@Nullable CategoryDto category, List<SelectItem> items) {
        return CategorySaveFlowParam.builder()
                .name(getString(category, CategoryDto::getName))
                .description(getString(category, CategoryDto::getDescription))
                .items(items)
                .parent(getNullString(category, CategoryDto::getParentName))
                .build();
    }
}
