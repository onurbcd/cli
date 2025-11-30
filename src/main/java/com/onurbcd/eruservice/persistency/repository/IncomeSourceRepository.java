package com.onurbcd.eruservice.persistency.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.onurbcd.eruservice.dto.ItemDto;
import com.onurbcd.eruservice.dto.incomesource.IncomeSourceDto;
import com.onurbcd.eruservice.persistency.entity.IncomeSource;

@Repository
public interface IncomeSourceRepository extends MiddlewareRepository<IncomeSource, IncomeSourceDto> {

    @Override
    @Modifying
    @Query("delete from IncomeSource i where i.id = :id")
    int deleteUsingId(UUID id);

    @Override
    @Modifying
    @Query("update IncomeSource i set i.active = :active where i.id = :id")
    int updateActive(UUID id, Boolean active);

    @Override
    @Query("select new com.onurbcd.eruservice.dto.ItemDto(i.id, i.name)" +
            " from IncomeSource i" +
            " where :id is null" +
            " or i.id != :id" +
            " order by i.name")
    List<ItemDto> getItems(UUID id);
}
