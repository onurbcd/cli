package com.onurbcd.eruservice.dto.billtype;

import static com.onurbcd.eruservice.util.Constant.CATEGORY_ID_ID;
import static com.onurbcd.eruservice.util.Constant.NAME_ID;
import static com.onurbcd.eruservice.util.Constant.PATH_ID;

import java.util.Optional;
import java.util.UUID;

import org.springframework.lang.Nullable;
import org.springframework.shell.component.context.ComponentContext;
import org.springframework.validation.annotation.Validated;

import com.onurbcd.eruservice.dto.PrimeSaveDto;
import com.onurbcd.eruservice.util.Constant;
import com.onurbcd.eruservice.util.Converter;
import com.onurbcd.eruservice.util.FlowUtil;
import com.onurbcd.eruservice.util.StringUtil;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
@Validated
public class BillTypeSaveDto extends PrimeSaveDto {

    @NotNull(message = "Path is required")
    @Size(min = 3, max = 250, message = "Path must be between 3 and 250 characters")
    @Pattern(regexp = Constant.REGEXP_PATH, message = "Path format is invalid")
    private String path;

    @NotNull(message = "Category is required")
    private UUID categoryId;

    public static BillTypeSaveDto of(ComponentContext<?> context, @Nullable BillTypeDto billType) {
        return BillTypeSaveDto
                .builder()
                .name(StringUtil.normalizeSpace(FlowUtil.getString(context, NAME_ID)))
                .active(Optional.ofNullable(billType).map(BillTypeDto::isActive).orElse(Boolean.TRUE))
                .path(StringUtil.normalizeSpace(FlowUtil.getString(context, PATH_ID)))
                .categoryId(Converter.toUUID(FlowUtil.getString(context, CATEGORY_ID_ID)))
                .build();
    }
}
