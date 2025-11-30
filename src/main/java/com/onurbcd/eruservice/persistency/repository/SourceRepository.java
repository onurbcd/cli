package com.onurbcd.eruservice.persistency.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.onurbcd.eruservice.dto.ItemDto;
import com.onurbcd.eruservice.dto.source.SourceDto;
import com.onurbcd.eruservice.enums.SourceType;
import com.onurbcd.eruservice.persistency.entity.Source;

@Repository
public interface SourceRepository extends EruRepository<Source, SourceDto> {

        @Override
        @Modifying
        @Query("delete from Source s where s.id = :id")
        int deleteUsingId(UUID id);

        @Override
        @Modifying
        @Query("update Source s set s.active = :active where s.id = :id")
        int updateActive(UUID id, Boolean active);

        @Query("select sum(s.balance)" +
                        " from Source s" +
                        " where s.sourceType = :sourceType")
        BigDecimal getBalanceSum(@Param("sourceType") SourceType sourceType);

        @Query("select sum(s.balance) from Source s")
        BigDecimal getBalanceSum();

        @Modifying
        @Query("update Source s" +
                        " set s.balance = :balance" +
                        " where s.id = :id")
        void updateBalance(@Param("id") UUID id, @Param("balance") BigDecimal balance);

        @Query("select s.balance" +
                        " from Source s" +
                        " where s.id = :id")
        BigDecimal getBalance(@Param("id") UUID id);

        @Override
        @Query("select new com.onurbcd.eruservice.dto.ItemDto(s.id, s.name)" +
                        " from Source s" +
                        " where :id is null" +
                        " or s.id != :id" +
                        " order by s.name")
        List<ItemDto> getItems(UUID id);
}
