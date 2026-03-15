package com.onurbcd.eruservice.dto.billtype;

import com.onurbcd.eruservice.dto.PrimeSaveDto;
import com.onurbcd.eruservice.util.Constant;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.lang.Nullable;
import org.springframework.shell.component.context.ComponentContext;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

import static com.onurbcd.eruservice.util.Constant.*;
import static com.onurbcd.eruservice.util.Converter.toUUID;
import static com.onurbcd.eruservice.util.FlowUtil.getString;
import static com.onurbcd.eruservice.util.ParamUtil.getBoolean;
import static com.onurbcd.eruservice.util.StringUtil.normalizeSpace;

@SuperBuilder
@Getter
@Setter
@Validated
public class BillTypeSaveDto extends PrimeSaveDto {

    @NotNull(message = "Path is required")
    @Size(min = 3, max = 250, message = "Path must be between {min} and {max} characters")
    @Pattern(regexp = Constant.REGEXP_PATH, message = "Path format is invalid")
    private String path;

    @NotNull(message = "Category is required")
    private UUID categoryId;

    public static BillTypeSaveDto of(ComponentContext<?> context, @Nullable BillTypeDto billType) {
        return BillTypeSaveDto.builder()
                .name(normalizeSpace(getString(context, NAME_ID)))
                .active(getBoolean(billType, BillTypeDto::isActive))
                .path(normalizeSpace(getString(context, PATH_ID)))
                .categoryId(toUUID(getString(context, CATEGORY_ID_ID)))
                .build();
    }
}
