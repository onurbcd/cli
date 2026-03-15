package com.onurbcd.cli.dto.day;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Builder
@Getter
@Setter
public class CreateMonthDto {

    private Short calendarYear;
    private Short calendarMonth;

    public static CreateMonthDto of(Short calendarYear, Short calendarMonth) {
        return CreateMonthDto
                .builder()
                .calendarYear(calendarYear)
                .calendarMonth(calendarMonth)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        var that = (CreateMonthDto) o;

        return new EqualsBuilder()
                .append(calendarYear, that.calendarYear)
                .append(calendarMonth, that.calendarMonth)
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
