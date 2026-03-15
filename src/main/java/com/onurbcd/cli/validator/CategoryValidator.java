package com.onurbcd.cli.validator;

import com.onurbcd.cli.annotation.Validator;
import com.onurbcd.cli.dto.category.CategoryDto;
import com.onurbcd.cli.dto.category.CategorySaveDto;
import com.onurbcd.cli.enums.Error;
import com.onurbcd.cli.util.NumberUtil;
import org.springframework.lang.Nullable;

import java.util.Objects;
import java.util.UUID;

@Validator
public class CategoryValidator {

    public void validate(CategorySaveDto saveDto, @Nullable CategoryDto current, @Nullable UUID id) {
        Action.checkIf(id != null || saveDto.getParentId() != null).orElseThrow(Error.CATEGORY_PARENT_IS_NULL);
        Action.checkIf(id == null || notLevelOne(current)).orElseThrow(Error.CATEGORY_LEVEL_ONE_IS_UNCHANGEABLE);

        Action.checkIf(id == null || saveDto.getParentId().equals(Objects.requireNonNull(current).getParentId()))
                .orElseThrow(Error.CATEGORY_PARENT_IS_UNCHANGEABLE);
    }

    public void validateDelete(CategoryDto dto) {
        Action.checkIf(notLevelOne(dto)).orElseThrow(Error.CATEGORY_CANNOT_DELETE_LEVEL_ONE);
        Action.checkIf(dto.getLastBranch()).orElseThrow(Error.CATEGORY_DELETE_NON_LAST_BRANCH);
    }

    private boolean notLevelOne(CategoryDto dto) {
        return NumberUtil.notEquals(Objects.requireNonNull(dto).getLevel(), Short.valueOf("1"));
    }
}
