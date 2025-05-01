package com.onurbcd.eruservice.service.impl;

import com.onurbcd.eruservice.annotation.PrimeService;
import com.onurbcd.eruservice.enums.Domain;
import com.onurbcd.eruservice.dto.Dtoable;
import com.onurbcd.eruservice.dto.secret.SecretDto;
import com.onurbcd.eruservice.dto.secret.SecretSaveDto;
import com.onurbcd.eruservice.persistency.entity.Entityable;
import com.onurbcd.eruservice.persistency.entity.Secret;
import com.onurbcd.eruservice.persistency.predicate.SecretPredicateBuilder;
import com.onurbcd.eruservice.persistency.repository.SecretRepository;
import com.onurbcd.eruservice.service.AbstractCrudService;
import com.onurbcd.eruservice.enums.QueryType;
import com.onurbcd.eruservice.service.helper.Cryptoable;
import com.onurbcd.eruservice.service.mapper.SecretToEntityMapper;
import com.onurbcd.eruservice.service.mapper.SecretToDtoMapper;
import com.onurbcd.eruservice.service.validation.SecretValidationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

import java.util.UUID;

@PrimeService(Domain.SECRET)
public class SecretService extends AbstractCrudService<Secret, SecretDto, SecretPredicateBuilder, SecretSaveDto> {

    private final Cryptoable cryptoable;

    private final SecretValidationService validationService;

    public SecretService(SecretRepository repository, Cryptoable cryptoable, SecretToDtoMapper toDtoMapper,
                         SecretToEntityMapper toEntityMapper, SecretValidationService validationService) {

        super(repository, toDtoMapper, toEntityMapper, QueryType.JPA, SecretPredicateBuilder.class);
        this.cryptoable = cryptoable;
        this.validationService = validationService;
    }

    @Override
    public void validate(Dtoable dto, @Nullable Entityable entity, @Nullable UUID id) {
        validationService.validate((SecretSaveDto) dto, (Secret) entity, id);
    }

    @Override
    public Secret fillValues(Dtoable dto, Entityable entity) {
        var secret = (Secret) super.fillValues(dto, entity);

        if (StringUtils.isNotBlank(secret.getPassword())) {
            secret.setPassword(cryptoable.encrypt(secret.getPassword()));
        }

        return secret;
    }
}
