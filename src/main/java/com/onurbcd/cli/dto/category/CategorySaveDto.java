package com.onurbcd.cli.dto.category;

import com.onurbcd.cli.dto.PrimeSaveDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.context.ComponentContext;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

import static com.onurbcd.cli.util.Constant.*;
import static com.onurbcd.cli.util.Converter.toUUID;
import static com.onurbcd.cli.util.FlowUtil.getString;
import static com.onurbcd.cli.util.ParamUtil.getBoolean;
import static com.onurbcd.cli.util.StringUtil.normalizeSpace;

@SuperBuilder
@Getter
@Setter
@Validated
public class CategorySaveDto extends PrimeSaveDto {

    @NotNull(message = "Parent id is required.")
    private UUID parentId;

    @Size(max = 250, message = "Description must be less than 250 characters.")
    private String description;

    public static CategorySaveDto of(ComponentContext<?> context, @Nullable CategoryDto category) {
        return CategorySaveDto.builder()
                .name(normalizeSpace(getString(context, NAME_ID)))
                .active(getBoolean(category, CategoryDto::isActive))
                .parentId(toUUID(getString(context, PARENT_ID_ID)))
                .description(normalizeSpace(getString(context, DESCRIPTION_ID)))
                .build();
    }
}
