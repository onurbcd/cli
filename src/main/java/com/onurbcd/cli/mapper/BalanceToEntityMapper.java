package com.onurbcd.cli.mapper;

import com.onurbcd.cli.dto.balance.BalanceSaveDto;
import com.onurbcd.cli.persistency.entity.Balance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = DefaultMapperConfig.class)
public interface BalanceToEntityMapper extends EntityMapper<BalanceSaveDto, Balance> {

    @Override
    @Mapping(source = "dayCalendarDate", target = "day.calendarDate")
    @Mapping(source = "sourceId", target = "source.id")
    @Mapping(source = "categoryId", target = "category.id")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "documents", ignore = true)
    Balance apply(BalanceSaveDto balanceSaveDto);
}
