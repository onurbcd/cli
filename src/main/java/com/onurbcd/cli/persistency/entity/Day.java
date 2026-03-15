package com.onurbcd.cli.persistency.entity;

import com.onurbcd.cli.annotation.MaxYear;
import com.onurbcd.cli.annotation.MinYear;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.domain.Persistable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Entity
@Table(name = "day", uniqueConstraints = {
        @UniqueConstraint(
                name = "uc_day_calendar_date",
                columnNames = {
                        "calendar_date"
                }
        )
})
@NoArgsConstructor
@Getter
@Setter
public class Day implements Persistable<Integer> {

    @Id
    @Column(name = "id", nullable = false)
    @Getter(AccessLevel.NONE)
    private Integer id;

    @NotNull
    @Column(name = "calendar_date", nullable = false, unique = true, updatable = false)
    private LocalDate calendarDate;

    @NotNull
    @MinYear
    @MaxYear
    @Column(name = "calendar_year", nullable = false, updatable = false)
    private Short calendarYear;

    @NotNull
    @Min(1)
    @Max(4)
    @Column(name = "calendar_quarter", nullable = false, updatable = false)
    private Short calendarQuarter;

    @NotNull
    @Min(1)
    @Max(12)
    @Column(name = "calendar_month", nullable = false, updatable = false)
    private Short calendarMonth;

    @NotNull
    @Size(min = 3, max = 9)
    @Column(name = "calendar_month_name", nullable = false, length = 9, updatable = false)
    private String calendarMonthName;

    @NotNull
    @Min(0)
    @Max(54)
    @Column(name = "calendar_week_in_year", nullable = false, updatable = false)
    private Short calendarWeekInYear;

    @NotNull
    @Min(0)
    @Max(6)
    @Column(name = "calendar_week_in_month", nullable = false, updatable = false)
    private Short calendarWeekInMonth;

    @NotNull
    @Min(1)
    @Max(366)
    @Column(name = "calendar_day_in_year", nullable = false, updatable = false)
    private Short calendarDayInYear;

    @NotNull
    @Min(1)
    @Max(7)
    @Column(name = "calendar_day_in_week", nullable = false, updatable = false)
    private Short calendarDayInWeek;

    @NotNull
    @Min(1)
    @Max(31)
    @Column(name = "calendar_day_in_month", nullable = false, updatable = false)
    private Short calendarDayInMonth;

    @NotNull
    @Size(min = 6, max = 9)
    @Column(name = "calendar_weekday_name", nullable = false, length = 9, updatable = false)
    private String calendarWeekdayName;

    public Day(Integer id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Day day = (Day) o;
        return new EqualsBuilder().append(id, day.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).toHashCode();
    }

    @Override
    public Integer getId() {
        return id;
    }

    /**
     * Prevent Spring Data doing a select-before-insert - this particular entity is never updated.
     *
     * @return true
     */
    @Override
    public boolean isNew() {
        return true;
    }
}
