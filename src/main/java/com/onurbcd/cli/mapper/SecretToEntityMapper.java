package com.onurbcd.cli.mapper;

import com.onurbcd.cli.dto.secret.SecretSaveDto;
import com.onurbcd.cli.persistency.entity.Secret;
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
