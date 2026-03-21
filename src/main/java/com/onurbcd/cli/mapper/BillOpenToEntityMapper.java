package com.onurbcd.cli.mapper;

import com.onurbcd.cli.dto.bill.BillOpenDto;
import com.onurbcd.cli.persistency.entity.Bill;
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
    @Mapping(source = "budgetId", target = "budget.id")
    @Mapping(target = "closed", ignore = true)
    @Mapping(target = "balance", ignore = true)
    Bill apply(BillOpenDto billOpenDto);
}
