package com.onurbcd.cli.persistency.repository;

import com.onurbcd.cli.dto.ItemDto;
import com.onurbcd.cli.dto.billtype.BillTypeDto;
import com.onurbcd.cli.dto.billtype.BillTypeValuesDto;
import com.onurbcd.cli.persistency.entity.BillType;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BillTypeRepository extends EruRepository<BillType, BillTypeDto> {

    @Override
    @Modifying
    @Query("delete from BillType b where b.id = :id")
    int deleteUsingId(UUID id);

    @Override
    @Modifying
    @Query("update BillType b set b.active = :active where b.id = :id")
    int updateActive(UUID id, Boolean active);

    @Override
    @Query("select b" +
            " from BillType b" +
            " inner join fetch b.category c" +
            " where b.id = :id")
    Optional<BillType> get(UUID id);

    @Query("select new com.onurbcd.cli.dto.billtype.BillTypeValuesDto(b.path, b.category.id)" +
            " from BillType b" +
            " where b.id = :id")
    Optional<BillTypeValuesDto> getValues(@Param("id") UUID id);

    @Override
    @Query("select new com.onurbcd.cli.dto.ItemDto(bt.id, bt.name)" +
            " from BillType bt" +
            " where :id is null" +
            " or bt.id != :id" +
            " order by bt.name")
    List<ItemDto> getItems(UUID id);
}
