package com.onurbcd.cli.mapper;

import com.onurbcd.cli.dto.source.SourceSaveDto;
import com.onurbcd.cli.persistency.entity.Source;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = DefaultMapperConfig.class)
public interface SourceToEntityMapper extends EntityMapper<SourceSaveDto, Source> {

    @Override
    @Mapping(source = "incomeSourceId", target = "incomeSource.id")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    Source apply(SourceSaveDto sourceSaveDto);
}
