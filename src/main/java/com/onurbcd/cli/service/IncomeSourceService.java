package com.onurbcd.cli.service;

import com.onurbcd.cli.annotation.PrimeService;
import com.onurbcd.cli.enums.Domain;
import com.onurbcd.cli.dto.incomesource.IncomeSourceDto;
import com.onurbcd.cli.dto.incomesource.IncomeSourceSaveDto;
import com.onurbcd.cli.persistency.entity.IncomeSource;
import com.onurbcd.cli.persistency.predicate.IncomeSourcePredicateBuilder;
import com.onurbcd.cli.persistency.repository.IncomeSourceRepository;
import com.onurbcd.cli.enums.QueryType;
import com.onurbcd.cli.mapper.IncomeSourceToDtoMapper;
import com.onurbcd.cli.mapper.IncomeSourceToEntityMapper;

@PrimeService(Domain.INCOME_SOURCE)
public class IncomeSourceService
        extends AbstractCrudService<IncomeSource, IncomeSourceDto, IncomeSourcePredicateBuilder, IncomeSourceSaveDto> {

    public IncomeSourceService(IncomeSourceRepository repository, IncomeSourceToDtoMapper toDtoMapper,
                               IncomeSourceToEntityMapper toEntityMapper) {

        super(repository, toDtoMapper, toEntityMapper, QueryType.JPA, IncomeSourcePredicateBuilder.class);
    }
}
