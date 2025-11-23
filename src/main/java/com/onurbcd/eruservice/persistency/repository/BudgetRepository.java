package com.onurbcd.eruservice.persistency.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.onurbcd.eruservice.dto.budget.BudgetDto;
import com.onurbcd.eruservice.dto.budget.BudgetSumDto;
import com.onurbcd.eruservice.dto.budget.BudgetValuesDto;
import com.onurbcd.eruservice.dto.budget.MonthlyBudgetDto;
import com.onurbcd.eruservice.model.SequenceParam;
import com.onurbcd.eruservice.persistency.entity.Budget;

@Repository
public interface BudgetRepository extends EruRepository<Budget, BudgetDto>, SequenceRepository {

        @Override
        @Query("select max(b.sequence)" +
                        " from Budget b" +
                        " where b.refYear = :#{#sequenceParam.year}" +
                        " and b.refMonth = :#{#sequenceParam.month}")
        Short getMaxSequence(SequenceParam sequenceParam);

        @Override
        @Query("select case b.id when null then false else true end" +
                        " from Budget b" +
                        " where b.refYear = :#{#sequenceParam.year}" +
                        " and b.refMonth = :#{#sequenceParam.month}" +
                        " and b.sequence = :#{#sequenceParam.sequence}")
        Boolean existsSequence(SequenceParam sequenceParam);

        @Override
        @Modifying
        @Query("update Budget b" +
                        " set b.sequence = :#{#sequenceParam.targetSequence}" +
                        " where b.refYear = :#{#sequenceParam.year}" +
                        " and b.refMonth = :#{#sequenceParam.month}" +
                        " and b.sequence = :#{#sequenceParam.sequence}")
        void updateSequence(SequenceParam sequenceParam);

        @Override
        @Query("select count(*)" +
                        " from Budget b" +
                        " where b.refYear = :#{#sequenceParam.year}" +
                        " and b.refMonth = :#{#sequenceParam.month}" +
                        " and b.sequence > :#{#sequenceParam.sequence}")
        long countNextSequences(SequenceParam sequenceParam);

        @Query("select new com.onurbcd.eruservice.dto.budget.BudgetSumDto(sum(b.amount), b.paid, count(*))" +
                        " from Budget b" +
                        " where b.refYear = :refYear" +
                        " and b.refMonth = :refMonth" +
                        " group by b.paid")
        Set<BudgetSumDto> getSumByMonth(@Param("refYear") Short refYear, @Param("refMonth") Short refMonth);

        @Modifying
        @Query("delete from Budget b where b.refYear = :refYear and b.refMonth = :refMonth")
        int deleteAll(@Param("refYear") Short refYear, @Param("refMonth") Short refMonth);

        @Override
        @Modifying
        @Query("update Budget b set b.active = :active where b.id = :id")
        int updateActive(UUID id, Boolean active);

        @Modifying
        @Query("update Budget b" +
                        " set b.paid = :paid, b.lastModifiedDate = :lastModifiedDate" +
                        " where b.id = :id")
        int updatePaid(
                        @Param("id") UUID id,
                        @Param("paid") Boolean paid,
                        @Param("lastModifiedDate") LocalDateTime lastModifiedDate);

        @Modifying
        @Query("update Budget b" +
                        " set b.active = :active, b.paid = :paid, b.lastModifiedDate = :lastModifiedDate" +
                        " where b.id = :id")
        int updateActiveAndPaid(
                        @Param("id") UUID id,
                        @Param("active") Boolean active,
                        @Param("paid") Boolean paid,
                        @Param("lastModifiedDate") LocalDateTime lastModifiedDate);

        @Query("select new com.onurbcd.eruservice.dto.budget.BudgetValuesDto(b.amount, b.billType.id, b.billType.path, b.paid)"
                        +
                        " from Budget b" +
                        " where b.id = :id")
        Optional<BudgetValuesDto> getBudgetValues(@Param("id") UUID id);

        @Query("select new com.onurbcd.eruservice.dto.budget.MonthlyBudgetDto(b.id, b.name, b.sequence)" +
                        " from Budget b" +
                        " where b.refYear = :refYear" +
                        " and b.refMonth = :refMonth" +
                        " order by b.sequence")
        List<MonthlyBudgetDto> getMonthlyBudget(@Param("refYear") Short refYear, @Param("refMonth") Short refMonth);
}
