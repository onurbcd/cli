package com.onurbcd.eruservice.dto.category;

import static com.onurbcd.eruservice.util.Constant.DESCRIPTION;
import static com.onurbcd.eruservice.util.Constant.NAME;
import static com.onurbcd.eruservice.util.Constant.PARENT_ID;

import java.util.Optional;
import java.util.UUID;

import org.springframework.lang.Nullable;
import org.springframework.shell.component.context.ComponentContext;

import com.onurbcd.eruservice.dto.PrimeSaveDto;
import com.onurbcd.eruservice.util.Converter;
import com.onurbcd.eruservice.util.StringUtil;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
public class CategorySaveDto extends PrimeSaveDto {

    private UUID parentId;
    private String description;

    public static CategorySaveDto of(ComponentContext<?> context, @Nullable CategoryDto category) {
        return CategorySaveDto
                .builder()
                .name(StringUtil.normalizeSpace(context.get(NAME, String.class)))
                .active(Optional.ofNullable(category).map(CategoryDto::isActive).orElse(Boolean.TRUE))
                .parentId(Converter.toUUID(context.get(PARENT_ID, String.class)))
                .description(StringUtil.normalizeSpace(context.get(DESCRIPTION, String.class)))
                .build();
    }
}
