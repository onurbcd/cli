package com.onurbcd.eruservice.mapper;

import com.onurbcd.eruservice.dto.secret.SecretSaveDto;
import com.onurbcd.eruservice.persistency.entity.Secret;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = DefaultMapperConfig.class)
public interface SecretToEntityMapper extends EntityMapper<SecretSaveDto, Secret> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    Secret apply(SecretSaveDto secretSaveDto);
}
