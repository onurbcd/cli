package com.onurbcd.eruservice.service.impl;

import com.onurbcd.eruservice.enums.SourceType;
import com.onurbcd.eruservice.dto.filter.Filterable;
import com.onurbcd.eruservice.dto.filter.SourceFilter;
import com.onurbcd.eruservice.dto.source.BalanceSumDto;
import com.onurbcd.eruservice.dto.source.SourceDto;
import com.onurbcd.eruservice.dto.source.SourceSaveDto;
import com.onurbcd.eruservice.persistency.entity.Source;
import com.onurbcd.eruservice.persistency.predicate.SourcePredicateBuilder;
import com.onurbcd.eruservice.persistency.repository.SourceRepository;
import com.onurbcd.eruservice.service.AbstractCrudService;
import com.onurbcd.eruservice.service.SourceService;
import com.onurbcd.eruservice.enums.QueryType;
import com.onurbcd.eruservice.mapper.SourceToEntityMapper;
import com.onurbcd.eruservice.model.UpdateSourceBalance;
import com.querydsl.core.types.Predicate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class SourceServiceImpl extends AbstractCrudService<Source, SourceDto, SourcePredicateBuilder, SourceSaveDto>
        implements SourceService {

    private final SourceRepository repository;

    public SourceServiceImpl(SourceRepository repository, SourceToEntityMapper toEntityMapper) {
        super(repository, toEntityMapper, QueryType.CUSTOM, SourcePredicateBuilder.class);
        this.repository = repository;
    }

    @Override
    protected Predicate getPredicate(Filterable filter) {
        return SourcePredicateBuilder.all((SourceFilter) filter);
    }

    @Override
    public BigDecimal getUsableBalanceSum() {
        var usableBalanceSum = repository.getBalanceSum(SourceType.USABLE);
        return usableBalanceSum != null ? usableBalanceSum : BigDecimal.ZERO;
    }

    @Override
    public BalanceSumDto getBalanceSum(SourceFilter filter) {
        var predicate = getPredicate(filter);
        var partial = repository.getSum(predicate);
        var total = repository.getBalanceSum();
        return new BalanceSumDto(partial, total);
    }

    @Override
    public void updateBalance(UpdateSourceBalance param) {
        var currentBalance = Optional
                .ofNullable(param.getSource().getBalance())
                .orElseGet(() -> repository.getBalance(param.getSource().getId()));

        var newBalance = param.getFunc().apply(currentBalance, param.getValue());
        repository.updateBalance(param.getSource().getId(), newBalance);
    }
}
