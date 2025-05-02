package com.onurbcd.eruservice.service;

import com.onurbcd.eruservice.annotation.PrimeService;
import com.onurbcd.eruservice.dto.Dtoable;
import com.onurbcd.eruservice.dto.secret.SecretDto;
import com.onurbcd.eruservice.dto.secret.SecretSaveDto;
import com.onurbcd.eruservice.enums.Domain;
import com.onurbcd.eruservice.enums.QueryType;
import com.onurbcd.eruservice.helper.CryptoHelper;
import com.onurbcd.eruservice.mapper.SecretToDtoMapper;
import com.onurbcd.eruservice.mapper.SecretToEntityMapper;
import com.onurbcd.eruservice.persistency.entity.Entityable;
import com.onurbcd.eruservice.persistency.entity.Secret;
import com.onurbcd.eruservice.persistency.predicate.SecretPredicateBuilder;
import com.onurbcd.eruservice.persistency.repository.SecretRepository;
import com.onurbcd.eruservice.validation.Action;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

import java.util.UUID;

@PrimeService(Domain.SECRET)
public class SecretService extends AbstractCrudService<Secret, SecretDto, SecretPredicateBuilder, SecretSaveDto> {

    private final CryptoHelper cryptoHelper;

    public SecretService(SecretRepository repository, CryptoHelper cryptoHelper, SecretToDtoMapper toDtoMapper,
                         SecretToEntityMapper toEntityMapper) {

        super(repository, toDtoMapper, toEntityMapper, QueryType.JPA, SecretPredicateBuilder.class);
        this.cryptoHelper = cryptoHelper;
    }

    @Override
    public void validate(Dtoable dto, @Nullable Entityable entity, @Nullable UUID id) {
        Action.checkIf(id == null || entity != null).orElseThrowNotFound(id);
    }

    @Override
    public Secret fillValues(Dtoable dto, Entityable entity) {
        var secret = (Secret) super.fillValues(dto, entity);

        if (StringUtils.isNotBlank(secret.getPassword())) {
            secret.setPassword(cryptoHelper.encrypt(secret.getPassword()));
        }

        return secret;
    }
}
