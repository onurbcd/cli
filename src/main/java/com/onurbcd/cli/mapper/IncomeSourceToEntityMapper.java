package com.onurbcd.cli.mapper;

import com.onurbcd.cli.dto.incomesource.IncomeSourceSaveDto;
import com.onurbcd.cli.persistency.entity.IncomeSource;
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
