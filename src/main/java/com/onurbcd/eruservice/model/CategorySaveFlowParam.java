package com.onurbcd.eruservice.model;

import java.util.List;
import java.util.Optional;

import org.springframework.lang.Nullable;
import org.springframework.shell.component.flow.SelectItem;
import org.springframework.shell.standard.ShellOption;

import com.onurbcd.eruservice.dto.category.CategoryDto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CategorySaveFlowParam {

    private String name;
    private String description;
    private List<SelectItem> items;
    private String parent;

    public static CategorySaveFlowParam of(@Nullable CategoryDto category, List<SelectItem> items) {
        return CategorySaveFlowParam
                .builder()
                .name(Optional.ofNullable(category).map(CategoryDto::getName).orElse(ShellOption.NULL))
                .description(Optional.ofNullable(category).map(CategoryDto::getDescription).orElse(ShellOption.NULL))
                .items(items)
                .parent(Optional.ofNullable(category).map(CategoryDto::getParentName).orElse(null))
                .build();
    }
}
