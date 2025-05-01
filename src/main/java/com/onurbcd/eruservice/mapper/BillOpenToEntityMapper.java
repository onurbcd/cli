package com.onurbcd.eruservice.mapper;

import com.onurbcd.eruservice.dto.bill.BillOpenDto;
import com.onurbcd.eruservice.persistency.entity.Bill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = DefaultMapperConfig.class)
public interface BillOpenToEntityMapper extends EntityMapper<BillOpenDto, Bill> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "referenceDay", ignore = true)
    @Mapping(target = "documentDate", ignore = true)
    @Mapping(target = "dueDate", ignore = true)
    @Mapping(target = "value", ignore = true)
    @Mapping(target = "paymentDate", ignore = true)
    @Mapping(target = "billDocument", ignore = true)
    @Mapping(target = "receipt", ignore = true)
    @Mapping(target = "billType", ignore = true)
    @Mapping(target = "paymentType", ignore = true)
    @Mapping(source = "budgetId", target = "budget.id")
    @Mapping(target = "source", ignore = true)
    @Mapping(target = "closed", ignore = true)
    @Mapping(target = "balance", ignore = true)
    Bill apply(BillOpenDto billOpenDto);
}
