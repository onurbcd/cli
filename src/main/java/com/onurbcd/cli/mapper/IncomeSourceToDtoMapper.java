package com.onurbcd.cli.mapper;

import com.onurbcd.cli.dto.incomesource.IncomeSourceDto;
import com.onurbcd.cli.persistency.entity.IncomeSource;
import org.mapstruct.Mapper;

import java.util.function.Function;

@Mapper(config = DefaultMapperConfig.class)
public interface IncomeSourceToDtoMapper extends Function<IncomeSource, IncomeSourceDto> {
}
