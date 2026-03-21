package com.onurbcd.cli.persistency.repository;

import com.onurbcd.cli.dto.bill.BillDto;
import com.onurbcd.cli.persistency.entity.Bill;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BillRepository extends EruRepository<Bill, BillDto> {

    @Override
    @Modifying
    @Query("delete" +
            " from Bill b" +
            " where b.id = :id")
    int deleteUsingId(UUID id);

    @Override
    @Modifying
    @Query("update Bill b" +
            " set b.active = :active" +
            " where b.id = :id")
    int updateActive(UUID id, Boolean active);

    @Override
    @Query("select b" +
            " from Bill b" +
            " inner join fetch b.referenceDay r" +
            " inner join fetch b.dueDate d" +
            " inner join fetch b.billType bt" +
            " inner join fetch b.budget bg" +
            " left join fetch b.documentDate dd" +
            " left join fetch b.paymentDate pd" +
            " left join fetch b.balance bl" +
            " where b.id = :id")
    Optional<Bill> get(UUID id);

    @Query("select count(*)" +
            " from Bill b" +
            " where b.budget.id = :budgetId")
    long countByBudgetId(@Param("budgetId") UUID budgetId);
}
