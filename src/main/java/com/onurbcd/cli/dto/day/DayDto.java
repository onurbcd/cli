package com.onurbcd.cli.dto.day;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DayDto {

    private Short calendarYear;

    private Short calendarMonth;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DayDto dayDto = (DayDto) o;

        return new EqualsBuilder()
                .append(calendarYear, dayDto.calendarYear)
                .append(calendarMonth, dayDto.calendarMonth)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(calendarYear)
                .append(calendarMonth)
                .toHashCode();
    }
}
