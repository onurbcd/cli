package com.onurbcd.eruservice.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.NullValueMappingStrategy;

@MapperConfig(componentModel = "spring", nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface DefaultMapperConfig {
}
