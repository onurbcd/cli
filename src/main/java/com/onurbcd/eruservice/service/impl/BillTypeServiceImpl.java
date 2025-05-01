package com.onurbcd.eruservice.service.impl;

import com.onurbcd.eruservice.dto.Dtoable;
import com.onurbcd.eruservice.dto.billtype.BillTypeDto;
import com.onurbcd.eruservice.dto.billtype.BillTypeSaveDto;
import com.onurbcd.eruservice.dto.billtype.BillTypeValuesDto;
import com.onurbcd.eruservice.persistency.entity.BillType;
import com.onurbcd.eruservice.persistency.predicate.BillTypePredicateBuilder;
import com.onurbcd.eruservice.persistency.repository.BillTypeRepository;
import com.onurbcd.eruservice.service.AbstractCrudService;
import com.onurbcd.eruservice.service.BillTypeService;
import com.onurbcd.eruservice.enums.QueryType;
import com.onurbcd.eruservice.exception.ApiException;
import com.onurbcd.eruservice.service.mapper.BillTypeToEntityMapper;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BillTypeServiceImpl
        extends AbstractCrudService<BillType, BillTypeDto, BillTypePredicateBuilder, BillTypeSaveDto>
        implements BillTypeService {

    private final BillTypeRepository repository;

    public BillTypeServiceImpl(BillTypeRepository repository, BillTypeToEntityMapper toEntityMapper) {
        super(repository, toEntityMapper, QueryType.CUSTOM, BillTypePredicateBuilder.class);
        this.repository = repository;
    }

    @Override
    public String save(Dtoable dto, UUID id) {
        var currentBillType = id != null ? repository.get(id).orElse(null) : null;
        validate(dto, currentBillType, id);
        var newBillType = (BillType) fillValues(dto, currentBillType);
        newBillType = repository.save(newBillType);
        return newBillType.getId().toString();
    }

    @Override
    public BillTypeValuesDto getValues(UUID id) {
        return repository
                .getValues(id)
                .orElseThrow(ApiException.notFound(id));
    }
}
