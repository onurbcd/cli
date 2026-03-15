package com.onurbcd.cli.persistency.repository;

import com.onurbcd.cli.dto.day.CreateMonthDto;
import com.onurbcd.cli.dto.day.DayDto;
import com.onurbcd.cli.persistency.entity.Day;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface DayRepository extends JpaRepository<Day, Integer> {

    @Query("select count(*)" +
            " from Day d" +
            " where d.calendarYear = :#{#dto.calendarYear}" +
            " and d.calendarMonth = :#{#dto.calendarMonth}")
    long numberOfDaysInMonth(@Param("dto") CreateMonthDto dto);

    @Query("select new com.onurbcd.cli.dto.day.DayDto(d.calendarYear, d.calendarMonth)" +
            " from Day d" +
            " group by d.calendarYear, d.calendarMonth" +
            " order by d.calendarYear, d.calendarMonth")
    Set<DayDto> getYearsAndMonths();
}
