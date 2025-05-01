package com.onurbcd.eruservice.service.validation.impl;

import com.onurbcd.eruservice.dto.category.CategoryDto;
import com.onurbcd.eruservice.dto.category.CategorySaveDto;
import com.onurbcd.eruservice.enums.Error;
import com.onurbcd.eruservice.service.validation.Action;
import com.onurbcd.eruservice.service.validation.CategoryValidationService;
import com.onurbcd.eruservice.util.NumberUtil;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class CategoryValidationServiceImpl implements CategoryValidationService {

    @Override
    public void validate(CategorySaveDto saveDto, CategoryDto current, UUID id) {
        Action.checkIf(id != null || saveDto.getParentId() != null).orElseThrow(Error.CATEGORY_PARENT_IS_NULL);
        Action.checkIf(id == null || notLevelOne(current)).orElseThrow(Error.CATEGORY_LEVEL_ONE_IS_UNCHANGEABLE);

        Action.checkIf(id == null || saveDto.getParentId().equals(current.getParentId()))
                .orElseThrow(Error.CATEGORY_PARENT_IS_UNCHANGEABLE);
    }

    @Override
    public void validateDelete(CategoryDto dto) {
        Action.checkIf(notLevelOne(dto)).orElseThrow(Error.CATEGORY_CANNOT_DELETE_LEVEL_ONE);
        Action.checkIf(dto.getLastBranch()).orElseThrow(Error.CATEGORY_DELETE_NON_LAST_BRANCH);
    }

    private boolean notLevelOne(CategoryDto dto) {
        return NumberUtil.notEquals(Objects.requireNonNull(dto).getLevel(), Short.valueOf("1"));
    }
}
