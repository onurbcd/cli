package com.onurbcd.cli.mapper;

import com.onurbcd.cli.dto.category.CategorySaveDto;
import com.onurbcd.cli.persistency.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = DefaultMapperConfig.class)
public interface CategoryToEntityMapper extends EntityMapper<CategorySaveDto, Category> {

    @Override
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent.id", source = "parentId")
    @Mapping(target = "level", ignore = true)
    @Mapping(target = "lastBranch", ignore = true)
    Category apply(CategorySaveDto categorySaveDto);
}
