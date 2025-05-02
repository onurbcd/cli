package com.onurbcd.eruservice.service;

import com.onurbcd.eruservice.dto.Dtoable;
import com.onurbcd.eruservice.dto.billtype.BillTypeDto;
import com.onurbcd.eruservice.dto.billtype.BillTypeSaveDto;
import com.onurbcd.eruservice.dto.billtype.BillTypeValuesDto;
import com.onurbcd.eruservice.enums.QueryType;
import com.onurbcd.eruservice.exception.ApiException;
import com.onurbcd.eruservice.mapper.BillTypeToEntityMapper;
import com.onurbcd.eruservice.persistency.entity.BillType;
import com.onurbcd.eruservice.persistency.predicate.BillTypePredicateBuilder;
import com.onurbcd.eruservice.persistency.repository.BillTypeRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BillTypeService extends AbstractCrudService<BillType, BillTypeDto, BillTypePredicateBuilder, BillTypeSaveDto>
        implements CrudService {

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
