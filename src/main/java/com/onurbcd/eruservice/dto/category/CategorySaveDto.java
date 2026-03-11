package com.onurbcd.eruservice.dto.category;

import static com.onurbcd.eruservice.util.Constant.DESCRIPTION_ID;
import static com.onurbcd.eruservice.util.Constant.NAME_ID;
import static com.onurbcd.eruservice.util.Constant.PARENT_ID_ID;

import java.util.Optional;
import java.util.UUID;

import org.springframework.lang.Nullable;
import org.springframework.shell.component.context.ComponentContext;
import org.springframework.validation.annotation.Validated;

import com.onurbcd.eruservice.dto.PrimeSaveDto;
import com.onurbcd.eruservice.util.Converter;
import com.onurbcd.eruservice.util.FlowUtil;
import com.onurbcd.eruservice.util.StringUtil;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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
        return CategorySaveDto
                .builder()
                .name(StringUtil.normalizeSpace(FlowUtil.getString(context, NAME_ID)))
                .active(Optional.ofNullable(category).map(CategoryDto::isActive).orElse(Boolean.TRUE))
                .parentId(Converter.toUUID(FlowUtil.getString(context, PARENT_ID_ID)))
                .description(StringUtil.normalizeSpace(FlowUtil.getString(context, DESCRIPTION_ID)))
                .build();
    }
}
