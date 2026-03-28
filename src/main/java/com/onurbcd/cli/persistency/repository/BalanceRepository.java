package com.onurbcd.cli.persistency.repository;

import com.onurbcd.cli.dto.balance.BalanceDto;
import com.onurbcd.cli.dto.document.DocumentDto;
import com.onurbcd.cli.param.SequenceParam;
import com.onurbcd.cli.persistency.entity.Balance;
import com.onurbcd.cli.persistency.entity.Document;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface BalanceRepository extends EruRepository<Balance, BalanceDto>, SequenceRepository {

    @Override
    @Modifying
    @Query("delete from Balance b where b.id = :id")
    int deleteUsingId(UUID id);

    @Override
    @Modifying
    @Query("update Balance b set b.active = :active where b.id = :id")
    int updateActive(UUID id, Boolean active);

    @Override
    @Query("select max(b.sequence)" +
            " from Balance b" +
            " inner join b.day d" +
            " where d.calendarYear = :#{#sequenceParam.year}" +
            " and d.calendarMonth = :#{#sequenceParam.month}" +
            " and b.balanceType = :#{#sequenceParam.balanceType}")
    Short getMaxSequence(SequenceParam sequenceParam);

    @Override
    @Query("select case b.id when null then false else true end" +
            " from Balance b" +
            " inner join b.day d" +
            " where d.calendarYear = :#{#sequenceParam.year}" +
            " and d.calendarMonth = :#{#sequenceParam.month}" +
            " and b.sequence = :#{#sequenceParam.sequence}" +
            " and b.balanceType = :#{#sequenceParam.balanceType}")
    Boolean existsSequence(SequenceParam sequenceParam);

    @Override
    @Modifying
    @Query("update Balance b1" +
            " set b1.sequence = :#{#sequenceParam.targetSequence}" +
            " where b1.id in (" +
            "   select b2.id" +
            "   from Balance b2" +
            "   inner join b2.day d" +
            "   where d.calendarYear = :#{#sequenceParam.year}" +
            "   and d.calendarMonth = :#{#sequenceParam.month}" +
            "   and b2.sequence = :#{#sequenceParam.sequence}" +
            "   and b2.balanceType = :#{#sequenceParam.balanceType})")
    void updateSequence(SequenceParam sequenceParam);

    @Override
    @Query("select count(*)" +
            " from Balance b" +
            " inner join b.day d" +
            " where d.calendarYear = :#{#sequenceParam.year}" +
            " and d.calendarMonth = :#{#sequenceParam.month}" +
            " and b.sequence > :#{#sequenceParam.sequence}" +
            " and b.balanceType = :#{#sequenceParam.balanceType}")
    long countNextSequences(SequenceParam sequenceParam);

    @Query("select new com.onurbcd.cli.dto.document.DocumentDto(d.id, d.name, d.path, d.mimeType, d.size, d.hash)" +
            " from Balance b" +
            " left join b.documents d" +
            " where b.id = :id")
    Set<DocumentDto> getDocumentsById(@Param("id") UUID id);

    @Query("select d" +
            " from Balance b" +
            " left join b.documents d" +
            " where b.id = :id")
    Set<Document> getDocuments(@Param("id") UUID id);

    @Override
    @Query("select b" +
            " from Balance b" +
            " inner join fetch b.day d" +
            " inner join fetch b.source s" +
            " inner join fetch b.category c" +
            " where b.id = :id")
    Optional<Balance> get(UUID id);
}
