package com.onurbcd.cli.mapper;

import com.onurbcd.cli.dto.billtype.BillTypeSaveDto;
import com.onurbcd.cli.persistency.entity.BillType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = DefaultMapperConfig.class)
public interface BillTypeToEntityMapper extends EntityMapper<BillTypeSaveDto, BillType> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(source = "categoryId", target = "category.id")
    BillType apply(BillTypeSaveDto billTypeSaveDto);
}
