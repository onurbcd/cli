package com.onurbcd.cli.service;

import com.onurbcd.cli.dto.filter.Filterable;
import com.onurbcd.cli.dto.filter.SourceFilter;
import com.onurbcd.cli.dto.source.BalanceSumDto;
import com.onurbcd.cli.dto.source.SourceDto;
import com.onurbcd.cli.dto.source.SourceSaveDto;
import com.onurbcd.cli.enums.QueryType;
import com.onurbcd.cli.enums.SourceType;
import com.onurbcd.cli.mapper.SourceToEntityMapper;
import com.onurbcd.cli.param.UpdateSourceBalance;
import com.onurbcd.cli.persistency.entity.Source;
import com.onurbcd.cli.persistency.predicate.SourcePredicateBuilder;
import com.onurbcd.cli.persistency.repository.SourceRepository;
import com.querydsl.core.types.Predicate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class SourceService extends AbstractCrudService<Source, SourceDto, SourcePredicateBuilder, SourceSaveDto> {

    private final SourceRepository repository;

    public SourceService(SourceRepository repository, SourceToEntityMapper toEntityMapper) {
        super(repository, toEntityMapper, QueryType.CUSTOM, SourcePredicateBuilder.class);
        this.repository = repository;
    }

    public BigDecimal getUsableBalanceSum() {
        var usableBalanceSum = repository.getBalanceSum(SourceType.USABLE);
        return usableBalanceSum != null ? usableBalanceSum : BigDecimal.ZERO;
    }

    public BalanceSumDto getBalanceSum(SourceFilter filter) {
        var predicate = getPredicate(filter);
        var partial = repository.getSum(predicate);
        var total = repository.getBalanceSum();
        return new BalanceSumDto(partial, total);
    }

    public void updateBalance(UpdateSourceBalance param) {
        var currentBalance = Optional
                .ofNullable(param.getSource().getBalance())
                .orElseGet(() -> repository.getBalance(param.getSource().getId()));

        var newBalance = param.getFunc().apply(currentBalance, param.getValue());
        repository.updateBalance(param.getSource().getId(), newBalance);
    }

    @Override
    protected Predicate getPredicate(Filterable filter) {
        return SourcePredicateBuilder.all((SourceFilter) filter);
    }
}
