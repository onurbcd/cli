package com.onurbcd.cli.mapper;

import com.onurbcd.cli.dto.secret.SecretDto;
import com.onurbcd.cli.helper.CryptoHelper;
import com.onurbcd.cli.persistency.entity.Secret;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.function.Function;

@Mapper(config = DefaultMapperConfig.class, uses = CryptoHelper.class,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface SecretToDtoMapper extends Function<Secret, SecretDto> {

    @Override
    @Mapping(target = "password", source = "password", qualifiedByName = "decrypt")
    SecretDto apply(Secret secret);
}
