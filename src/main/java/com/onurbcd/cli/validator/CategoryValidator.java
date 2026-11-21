package com.onurbcd.cli.validator;

import com.onurbcd.cli.annotation.Validator;
import com.onurbcd.cli.dto.category.CategoryDto;
import com.onurbcd.cli.dto.category.CategorySaveDto;
import com.onurbcd.cli.enums.ErrorCode;
import com.onurbcd.cli.util.NumberUtil;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

@Validator
public class CategoryValidator {

    public void validate(CategorySaveDto saveDto, @Nullable CategoryDto current, @Nullable UUID id) {
        Action.checkIf(id != null || saveDto.getParentId() != null).orElseThrow(ErrorCode.CATEGORY_PARENT_IS_NULL);
        Action.checkIf(id == null || notLevelOne(current)).orElseThrow(ErrorCode.CATEGORY_LEVEL_ONE_IS_UNCHANGEABLE);

        Action.checkIf(id == null || saveDto.getParentId().equals(Objects.requireNonNull(current).getParentId()))
                .orElseThrow(ErrorCode.CATEGORY_PARENT_IS_UNCHANGEABLE);
    }

    public void validateDelete(CategoryDto dto) {
        Action.checkIf(notLevelOne(dto)).orElseThrow(ErrorCode.CATEGORY_CANNOT_DELETE_LEVEL_ONE);
        Action.checkIf(dto.getLastBranch()).orElseThrow(ErrorCode.CATEGORY_DELETE_NON_LAST_BRANCH);
    }

    private boolean notLevelOne(CategoryDto dto) {
        return NumberUtil.notEquals(Objects.requireNonNull(dto).getLevel(), Short.valueOf("1"));
    }
}
