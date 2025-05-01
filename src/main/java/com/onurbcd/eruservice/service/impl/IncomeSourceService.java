package com.onurbcd.eruservice.service.impl;

import com.onurbcd.eruservice.annotation.PrimeService;
import com.onurbcd.eruservice.enums.Domain;
import com.onurbcd.eruservice.dto.incomesource.IncomeSourceDto;
import com.onurbcd.eruservice.dto.incomesource.IncomeSourceSaveDto;
import com.onurbcd.eruservice.persistency.entity.IncomeSource;
import com.onurbcd.eruservice.persistency.predicate.IncomeSourcePredicateBuilder;
import com.onurbcd.eruservice.persistency.repository.IncomeSourceRepository;
import com.onurbcd.eruservice.service.AbstractCrudService;
import com.onurbcd.eruservice.enums.QueryType;
import com.onurbcd.eruservice.service.mapper.IncomeSourceToDtoMapper;
import com.onurbcd.eruservice.service.mapper.IncomeSourceToEntityMapper;

@PrimeService(Domain.INCOME_SOURCE)
public class IncomeSourceService
        extends AbstractCrudService<IncomeSource, IncomeSourceDto, IncomeSourcePredicateBuilder, IncomeSourceSaveDto> {

    public IncomeSourceService(IncomeSourceRepository repository, IncomeSourceToDtoMapper toDtoMapper,
                               IncomeSourceToEntityMapper toEntityMapper) {

        super(repository, toDtoMapper, toEntityMapper, QueryType.JPA, IncomeSourcePredicateBuilder.class);
    }
}
