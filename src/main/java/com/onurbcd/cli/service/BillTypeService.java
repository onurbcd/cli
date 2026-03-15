package com.onurbcd.cli.service;

import com.onurbcd.cli.dto.Dtoable;
import com.onurbcd.cli.dto.billtype.BillTypeDto;
import com.onurbcd.cli.dto.billtype.BillTypeSaveDto;
import com.onurbcd.cli.dto.billtype.BillTypeValuesDto;
import com.onurbcd.cli.enums.QueryType;
import com.onurbcd.cli.exception.ApiException;
import com.onurbcd.cli.mapper.BillTypeToEntityMapper;
import com.onurbcd.cli.persistency.entity.BillType;
import com.onurbcd.cli.persistency.predicate.BillTypePredicateBuilder;
import com.onurbcd.cli.persistency.repository.BillTypeRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BillTypeService extends AbstractCrudService<BillType, BillTypeDto, BillTypePredicateBuilder, BillTypeSaveDto> {

    private final BillTypeRepository repository;

    public BillTypeService(BillTypeRepository repository, BillTypeToEntityMapper toEntityMapper) {
        super(repository, toEntityMapper, QueryType.CUSTOM, BillTypePredicateBuilder.class);
        this.repository = repository;
    }

    public BillTypeValuesDto getValues(UUID id) {
        return repository
                .getValues(id)
                .orElseThrow(ApiException.notFound(id));
    }

    @Override
    public String save(Dtoable dto, UUID id) {
        var currentBillType = id != null ? repository.get(id).orElse(null) : null;
        validate(dto, currentBillType, id);
        var newBillType = (BillType) fillValues(dto, currentBillType);
        newBillType = repository.save(newBillType);
        return newBillType.getId().toString();
    }
}
