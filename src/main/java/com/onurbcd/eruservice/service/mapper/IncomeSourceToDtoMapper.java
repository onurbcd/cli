package com.onurbcd.eruservice.service.mapper;

import com.onurbcd.eruservice.dto.incomesource.IncomeSourceDto;
import com.onurbcd.eruservice.mapper.DefaultMapperConfig;
import com.onurbcd.eruservice.persistency.entity.IncomeSource;
import org.mapstruct.Mapper;

import java.util.function.Function;

@Mapper(config = DefaultMapperConfig.class)
public interface IncomeSourceToDtoMapper extends Function<IncomeSource, IncomeSourceDto> {
}
