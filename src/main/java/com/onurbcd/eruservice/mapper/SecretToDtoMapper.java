package com.onurbcd.eruservice.mapper;

import com.onurbcd.eruservice.dto.secret.SecretDto;
import com.onurbcd.eruservice.helper.CryptoHelper;
import com.onurbcd.eruservice.persistency.entity.Secret;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Function;

@Mapper(config = DefaultMapperConfig.class)
public abstract class SecretToDtoMapper implements Function<Secret, SecretDto> {

    @Autowired
    protected CryptoHelper cryptoHelper;

    @Override
    @Mapping(target = "password", expression = "java(cryptoHelper.decrypt(secret.getPassword()))")
    public abstract SecretDto apply(Secret secret);
}
