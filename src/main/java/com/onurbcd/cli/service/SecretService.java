package com.onurbcd.cli.service;

import com.onurbcd.cli.annotation.PrimeService;
import com.onurbcd.cli.dto.Dtoable;
import com.onurbcd.cli.dto.secret.SecretDto;
import com.onurbcd.cli.dto.secret.SecretSaveDto;
import com.onurbcd.cli.enums.Domain;
import com.onurbcd.cli.enums.QueryType;
import com.onurbcd.cli.helper.CryptoHelper;
import com.onurbcd.cli.mapper.SecretToDtoMapper;
import com.onurbcd.cli.mapper.SecretToEntityMapper;
import com.onurbcd.cli.persistency.entity.Entityable;
import com.onurbcd.cli.persistency.entity.Secret;
import com.onurbcd.cli.persistency.predicate.SecretPredicateBuilder;
import com.onurbcd.cli.persistency.repository.SecretRepository;
import com.onurbcd.cli.validator.Action;
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
