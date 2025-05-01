package com.onurbcd.eruservice.service.mapper;

import com.onurbcd.eruservice.dto.incomesource.IncomeSourceSaveDto;
import com.onurbcd.eruservice.mapper.DefaultMapperConfig;
import com.onurbcd.eruservice.mapper.EntityMapper;
import com.onurbcd.eruservice.persistency.entity.IncomeSource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = DefaultMapperConfig.class)
public interface IncomeSourceToEntityMapper extends EntityMapper<IncomeSourceSaveDto, IncomeSource> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    IncomeSource apply(IncomeSourceSaveDto incomeSourceSaveDto);
}
