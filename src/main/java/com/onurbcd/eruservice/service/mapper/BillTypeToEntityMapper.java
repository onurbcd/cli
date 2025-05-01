package com.onurbcd.eruservice.service.mapper;

import com.onurbcd.eruservice.dto.billtype.BillTypeSaveDto;
import com.onurbcd.eruservice.mapper.DefaultMapperConfig;
import com.onurbcd.eruservice.mapper.EntityMapper;
import com.onurbcd.eruservice.persistency.entity.BillType;
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
